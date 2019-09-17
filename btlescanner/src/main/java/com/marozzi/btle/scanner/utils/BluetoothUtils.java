package com.marozzi.btle.scanner.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.SparseArray;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;
import com.marozzi.btle.scanner.model.Beacon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by amarozzi on 14/02/2018.
 */
public final class BluetoothUtils {

    private static final int DATA_TYPE_FLAGS = 0x01;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    private static final int DATA_TYPE_SERVICE_DATA = 0x16;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    private BluetoothUtils() {
    }

    /**
     * Determine weather the device has BLE.
     *
     * @param context where from you determine if the device has BLE
     * @return True if the device has BLE.
     */
    public static boolean hasBluetoothLE(final Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Determine if bluetooth is turned on.
     *
     * @return True if bloetuuth is turned on.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static boolean isBluetoothOn() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static Beacon parse(@NonNull BluetoothDevice device, int rssi, byte[] scanRecord, List<ParcelUuid> services) {
        UUID uuid = UUID.fromString("7AA9F224-D71E-4758-8F60-AA013261B9AC"); //standard technogym machine
        int major = 1;
        int minor = 1;
        int power = 0;

        // Parse the payload of the advertising packet.
        List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanRecord);
        if (structures != null && structures.size() > 0) {
            // For each AD structure contained in the advertising packet.
            for (ADStructure structure : structures) {
                if (structure instanceof IBeacon) {
                    // iBeacon was found.
                    IBeacon iBeacon = (IBeacon) structure;

                    // Proximity UUID, major number, minor number and power.
                    uuid = iBeacon.getUUID();
                    major = iBeacon.getMajor();
                    minor = iBeacon.getMinor();
                    power = iBeacon.getPower();
                    break;
                }
            }
        }

        return Beacon.newBuilder()
                .setUUID(uuid)
                .setMajor(major)
                .setMinor(minor)
                .setRssi(rssi)
                .setPower(power)
                .setDevice(device)
                .setServices(services)
                .build();
    }

    public static List<ParcelUuid> getServicesFromBytes(byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }

        int currentPos = 0;
        int advertiseFlag = -1;
        List<ParcelUuid> serviceUuids = new ArrayList<ParcelUuid>();
        String localName = null;
        int txPowerLevel = Integer.MIN_VALUE;

        SparseArray<byte[]> manufacturerData = new SparseArray<byte[]>();
        Map<ParcelUuid, byte[]> serviceData = new HashMap<ParcelUuid, byte[]>();

        try {
            while (currentPos < scanRecord.length) {
                // length is unsigned int.
                int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                int dataLength = length - 1;
                // fieldType is unsigned int.
                int fieldType = scanRecord[currentPos++] & 0xFF;
                switch (fieldType) {
                    case DATA_TYPE_FLAGS:
                        advertiseFlag = scanRecord[currentPos] & 0xFF;
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                        parseServiceUuid(scanRecord, currentPos,
                                dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                        parseServiceUuid(scanRecord, currentPos, dataLength,
                                BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                        parseServiceUuid(scanRecord, currentPos, dataLength,
                                BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_LOCAL_NAME_SHORT:
                    case DATA_TYPE_LOCAL_NAME_COMPLETE:
                        localName = new String(
                                extractBytes(scanRecord, currentPos, dataLength));
                        break;
                    case DATA_TYPE_TX_POWER_LEVEL:
                        txPowerLevel = scanRecord[currentPos];
                        break;
                    case DATA_TYPE_SERVICE_DATA:
                        // The first two bytes of the service data are service data UUID in little
                        // endian. The rest bytes are service data.
                        int serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT;
                        byte[] serviceDataUuidBytes = extractBytes(scanRecord, currentPos,
                                serviceUuidLength);
                        ParcelUuid serviceDataUuid = BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes);
                        byte[] serviceDataArray = extractBytes(scanRecord,
                                currentPos + serviceUuidLength, dataLength - serviceUuidLength);
                        serviceData.put(serviceDataUuid, serviceDataArray);
                        break;
                    case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        int manufacturerId = ((scanRecord[currentPos + 1] & 0xFF) << 8) +
                                (scanRecord[currentPos] & 0xFF);
                        byte[] manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2,
                                dataLength - 2);
                        manufacturerData.put(manufacturerId, manufacturerDataBytes);
                        break;
                    default:
                        // Just ignore, we don't handle such data type.
                        break;
                }
                currentPos += dataLength;
            }

            if (serviceUuids.isEmpty()) {
                serviceUuids = null;
            }
            return serviceUuids;

        } catch (Exception e) {
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw scanRecord bytes in results
            return null;
        }
    }

    // Parse service UUIDs.
    private static int parseServiceUuid(byte[] scanRecord, int currentPos, int dataLength,
                                        int uuidLength, List<ParcelUuid> serviceUuids) {
        while (dataLength > 0) {
            byte[] uuidBytes = extractBytes(scanRecord, currentPos,
                    uuidLength);
            serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes));
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }
        return currentPos;
    }

    // Helper method to extract bytes from byte array.
    private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    public static final class BluetoothUuid {
        /*
         * See Bluetooth Assigned Numbers document - SDP section, to get the values of UUIDs for the
         * various services. The following 128 bit values are calculated as: uuid * 2^96 + BASE_UUID
         */
        public static final ParcelUuid AudioSink =
                ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid AudioSource =
                ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid AdvAudioDist =
                ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid HSP =
                ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid HSP_AG =
                ParcelUuid.fromString("00001112-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid Handsfree =
                ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid Handsfree_AG =
                ParcelUuid.fromString("0000111F-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid AvrcpController =
                ParcelUuid.fromString("0000110E-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid AvrcpTarget =
                ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid ObexObjectPush =
                ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb");
        public static final ParcelUuid Hid =
                ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb");
        public static final ParcelUuid Hogp =
                ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb");
        public static final ParcelUuid PANU =
                ParcelUuid.fromString("00001115-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid NAP =
                ParcelUuid.fromString("00001116-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid BNEP =
                ParcelUuid.fromString("0000000f-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid PBAP_PSE =
                ParcelUuid.fromString("0000112f-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid MAP =
                ParcelUuid.fromString("00001134-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid MNS =
                ParcelUuid.fromString("00001133-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid MAS =
                ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB");
        public static final ParcelUuid BASE_UUID =
                ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
        /**
         * Length of bytes for 16 bit UUID
         */
        public static final int UUID_BYTES_16_BIT = 2;
        /**
         * Length of bytes for 32 bit UUID
         */
        public static final int UUID_BYTES_32_BIT = 4;
        /**
         * Length of bytes for 128 bit UUID
         */
        public static final int UUID_BYTES_128_BIT = 16;
        public static final ParcelUuid[] RESERVED_UUIDS = {
                AudioSink, AudioSource, AdvAudioDist, HSP, Handsfree, AvrcpController, AvrcpTarget,
                ObexObjectPush, PANU, NAP, MAP, MNS, MAS};

        public static boolean isAudioSource(ParcelUuid uuid) {
            return uuid.equals(AudioSource);
        }

        public static boolean isAudioSink(ParcelUuid uuid) {
            return uuid.equals(AudioSink);
        }

        public static boolean isAdvAudioDist(ParcelUuid uuid) {
            return uuid.equals(AdvAudioDist);
        }

        public static boolean isHandsfree(ParcelUuid uuid) {
            return uuid.equals(Handsfree);
        }

        public static boolean isHeadset(ParcelUuid uuid) {
            return uuid.equals(HSP);
        }

        public static boolean isAvrcpController(ParcelUuid uuid) {
            return uuid.equals(AvrcpController);
        }

        public static boolean isAvrcpTarget(ParcelUuid uuid) {
            return uuid.equals(AvrcpTarget);
        }

        public static boolean isInputDevice(ParcelUuid uuid) {
            return uuid.equals(Hid);
        }

        public static boolean isPanu(ParcelUuid uuid) {
            return uuid.equals(PANU);
        }

        public static boolean isNap(ParcelUuid uuid) {
            return uuid.equals(NAP);
        }

        public static boolean isBnep(ParcelUuid uuid) {
            return uuid.equals(BNEP);
        }

        public static boolean isMap(ParcelUuid uuid) {
            return uuid.equals(MAP);
        }

        public static boolean isMns(ParcelUuid uuid) {
            return uuid.equals(MNS);
        }

        public static boolean isMas(ParcelUuid uuid) {
            return uuid.equals(MAS);
        }

        /**
         * Returns true if ParcelUuid is present in uuidArray
         *
         * @param uuidArray - Array of ParcelUuids
         * @param uuid
         */
        public static boolean isUuidPresent(ParcelUuid[] uuidArray, ParcelUuid uuid) {
            if ((uuidArray == null || uuidArray.length == 0) && uuid == null) {
                return true;
            }
            if (uuidArray == null) {
                return false;
            }
            for (ParcelUuid element : uuidArray) {
                if (element.equals(uuid)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns true if there any common ParcelUuids in uuidA and uuidB.
         *
         * @param uuidA - List of ParcelUuids
         * @param uuidB - List of ParcelUuids
         */
        public static boolean containsAnyUuid(ParcelUuid[] uuidA, ParcelUuid[] uuidB) {
            if (uuidA == null && uuidB == null) {
                return true;
            }
            if (uuidA == null) {
                return uuidB.length == 0 ? true : false;
            }
            if (uuidB == null) {
                return uuidA.length == 0 ? true : false;
            }
            HashSet<ParcelUuid> uuidSet = new HashSet<ParcelUuid>(Arrays.asList(uuidA));
            for (ParcelUuid uuid : uuidB) {
                if (uuidSet.contains(uuid)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns true if all the ParcelUuids in ParcelUuidB are present in ParcelUuidA
         *
         * @param uuidA - Array of ParcelUuidsA
         * @param uuidB - Array of ParcelUuidsB
         */
        public static boolean containsAllUuids(ParcelUuid[] uuidA, ParcelUuid[] uuidB) {
            if (uuidA == null && uuidB == null) {
                return true;
            }
            if (uuidA == null) {
                return uuidB.length == 0 ? true : false;
            }
            if (uuidB == null) {
                return true;
            }
            HashSet<ParcelUuid> uuidSet = new HashSet<ParcelUuid>(Arrays.asList(uuidA));
            for (ParcelUuid uuid : uuidB) {
                if (!uuidSet.contains(uuid)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Extract the Service Identifier or the actual uuid from the Parcel Uuid. For example, if
         * 0000110B-0000-1000-8000-00805F9B34FB is the parcel Uuid, this function will return 110B
         *
         * @param parcelUuid
         * @return the service identifier.
         */
        public static int getServiceIdentifierFromParcelUuid(ParcelUuid parcelUuid) {
            UUID uuid = parcelUuid.getUuid();
            long value = (uuid.getMostSignificantBits() & 0x0000FFFF00000000L) >>> 32;
            return (int) value;
        }

        /**
         * Parse UUID from bytes. The {@code uuidBytes} can represent a 16-bit, 32-bit or 128-bit UUID,
         * but the returned UUID is always in 128-bit format. Note UUID is little endian in Bluetooth.
         *
         * @param uuidBytes Byte representation of uuid.
         * @return {@link ParcelUuid} parsed from bytes.
         * @throws IllegalArgumentException If the {@code uuidBytes} cannot be parsed.
         */
        public static ParcelUuid parseUuidFrom(byte[] uuidBytes) {
            if (uuidBytes == null) {
                throw new IllegalArgumentException("uuidBytes cannot be null");
            }
            int length = uuidBytes.length;
            if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT &&
                    length != UUID_BYTES_128_BIT) {
                throw new IllegalArgumentException("uuidBytes length invalid - " + length);
            }
            // Construct a 128 bit UUID.
            if (length == UUID_BYTES_128_BIT) {
                ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
                long msb = buf.getLong(8);
                long lsb = buf.getLong(0);
                return new ParcelUuid(new UUID(msb, lsb));
            }
            // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
            // 128_bit_value = uuid * 2^96 + BASE_UUID
            long shortUuid;
            if (length == UUID_BYTES_16_BIT) {
                shortUuid = uuidBytes[0] & 0xFF;
                shortUuid += (uuidBytes[1] & 0xFF) << 8;
            } else {
                shortUuid = uuidBytes[0] & 0xFF;
                shortUuid += (uuidBytes[1] & 0xFF) << 8;
                shortUuid += (uuidBytes[2] & 0xFF) << 16;
                shortUuid += (uuidBytes[3] & 0xFF) << 24;
            }
            long msb = BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32);
            long lsb = BASE_UUID.getUuid().getLeastSignificantBits();
            return new ParcelUuid(new UUID(msb, lsb));
        }

        /**
         * Check whether the given parcelUuid can be converted to 16 bit bluetooth uuid.
         *
         * @param parcelUuid
         * @return true if the parcelUuid can be converted to 16 bit uuid, false otherwise.
         */
        public static boolean is16BitUuid(ParcelUuid parcelUuid) {
            UUID uuid = parcelUuid.getUuid();
            if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
                return false;
            }
            return ((uuid.getMostSignificantBits() & 0xFFFF0000FFFFFFFFL) == 0x1000L);
        }

        /**
         * Check whether the given parcelUuid can be converted to 32 bit bluetooth uuid.
         *
         * @param parcelUuid
         * @return true if the parcelUuid can be converted to 32 bit uuid, false otherwise.
         */
        public static boolean is32BitUuid(ParcelUuid parcelUuid) {
            UUID uuid = parcelUuid.getUuid();
            if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
                return false;
            }
            if (is16BitUuid(parcelUuid)) {
                return false;
            }
            return ((uuid.getMostSignificantBits() & 0xFFFFFFFFL) == 0x1000L);
        }
    }
}
