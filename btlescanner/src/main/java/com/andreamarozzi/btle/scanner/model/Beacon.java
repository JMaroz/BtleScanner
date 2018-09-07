package com.andreamarozzi.btle.scanner.model;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.andreamarozzi.btle.scanner.interfaces.iBeacon;
import com.andreamarozzi.btle.scanner.utils.ConversionUtils;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;

/**
 * Created by amarozzi on 14/02/2018.
 */

public class Beacon implements iBeacon, Parcelable {

    private static final int MANUFACTURER_ID = 76;
    private static final int MAJOR_MINOR_MIN_VALUE = 0;
    private static final int MAJOR_MINOR_MAX_VALUE = 65535;

    private UUID uuid;
    private int major;
    private int minor;
    private int rssi;
    private int power;
    private BluetoothDevice device;
    private List<ParcelUuid> services;

    public Beacon() {
    }

    public Beacon(Builder builder) {
        this.uuid = builder.uuid;
        this.major = builder.major;
        this.minor = builder.minor;
        this.rssi = builder.rssi;
        this.power = builder.power;
        this.device = builder.device;
        this.services = builder.services;
    }

    @NonNull
    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public int getMajor() {
        return this.major;
    }

    @Override
    public int getMinor() {
        return this.minor;
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    @Override
    public int getPower() {
        return power;
    }

    @Override
    public BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public List<ParcelUuid> getServices() {
        return services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beacon beacon = (Beacon) o;

        if (major != beacon.major) return false;
        if (minor != beacon.minor) return false;
        if (rssi != beacon.rssi) return false;
        if (power != beacon.power) return false;
        if (uuid != null ? !uuid.equals(beacon.uuid) : beacon.uuid != null) return false;
        if (device != null ? !device.equals(beacon.device) : beacon.device != null) return false;
        return services != null ? services.equals(beacon.services) : beacon.services == null;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + major;
        result = 31 * result + minor;
        result = 31 * result + rssi;
        result = 31 * result + power;
        result = 31 * result + (device != null ? device.hashCode() : 0);
        result = 31 * result + (services != null ? services.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "uuid=" + uuid +
                ", major=" + major +
                ", minor=" + minor +
                ", rssi=" + rssi +
                ", power=" + power +
                ", device=" + (device != null ? device.toString() : "") +
                ", services=" + services +
                '}';
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanFilter toScanFilter() {
        final ScanFilter.Builder builder = new ScanFilter.Builder();

        // the manufacturer data byte is the filter!
        final byte[] manufacturerData = new byte[]{
                // identify as iBeacon
                (byte) 0x02, (byte) 0x15,

                // uuid
                0, 0, 0, 0,
                0, 0,
                0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,

                // major
                0, 0,

                // minor
                0, 0,

                0
        };

        // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
       final byte[] manufacturerDataMask;
        if (major != 0)
            manufacturerDataMask = new byte[]{
                    // Type and length
                    (byte) 0xFF, (byte) 0xFF,

                    // uuid
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,

                    // major
                    (byte) 0xFF, (byte) 0xFF,

                    // minor
                    0, 0, //(byte) 0xFF, (byte) 0xFF,

                    0
            };
        else
            manufacturerDataMask = new byte[]{
                    // Type and length
                    (byte) 0xFF, (byte) 0xFF,

                    // uuid
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,

                    // major
                    0, 0,

                    // minor
                    0, 0, //(byte) 0xFF, (byte) 0xFF,

                    0
            };


        // copy UUID (with no dashes) into data array
        System.arraycopy(ConversionUtils.UuidToByteArray(uuid), 0, manufacturerData, 2, 16);

        // copy major into data array
        System.arraycopy(ConversionUtils.integerToByteArray(major), 0, manufacturerData, 18, 2);

        // copy minor into data array
        System.arraycopy(ConversionUtils.integerToByteArray(minor), 0, manufacturerData, 20, 2);

        builder.setManufacturerData(MANUFACTURER_ID, manufacturerData, manufacturerDataMask);

        return builder.build();
    }

    /**
     * @return a new {@link Builder} to create a {@link Beacon}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A Builder class to create a {@link Beacon} and validate the {@link #uuid}, {@link #major} and {@link #minor}.
     */
    public static final class Builder {
        private UUID uuid;
        private int major;
        private int minor;
        private int rssi;
        private int power;
        private BluetoothDevice device;
        private List<ParcelUuid> services;

        private Builder() {
        }

        /**
         * @param uuid
         * @return
         */
        public Builder setUUID(@NonNull final UUID uuid) {
            this.uuid = uuid;

            return this;
        }

        /**
         * @param uuid that will get parsed with {@link UUID#fromString(String)}
         * @return
         * @throws IllegalArgumentException If name does not conform to the string representation as
         *                                  described in {@link UUID#toString}
         */
        public Builder setUUID(@NonNull final String uuid) throws IllegalArgumentException {
            this.uuid = UUID.fromString(uuid);

            return this;
        }

        /**
         * Major should be an integer between 0 and {@link #MAJOR_MINOR_MAX_VALUE}. By using 0 as
         * major, it will trigger for any major.
         *
         * @param major
         * @return
         */
        public Builder setMajor(final int major) {
            this.major = major;

            return this;
        }

        /**
         * Minor should be an integer between 0 and {@link #MAJOR_MINOR_MAX_VALUE}. By using 0 as
         * minor, it will trigger for any minor.
         *
         * @param minor
         * @return
         */
        public Builder setMinor(final int minor) {
            this.minor = minor;

            return this;
        }

        public Builder setRssi(int rssi) {
            this.rssi = rssi;
            return this;
        }

        public Builder setPower(int power) {
            this.power = power;
            return this;
        }

        public Builder setDevice(BluetoothDevice device) {
            this.device = device;
            return this;
        }

        public Builder setServices(List<ParcelUuid> services) {
            this.services = services;
            return this;
        }

        /**
         * If {@link #uuid}, {@link #major} and {@link #minor} are valid, build returns a new {@link Beacon} object.
         *
         * @return {@link Beacon} if the parameters are valid
         * @throws IllegalArgumentException
         */
        public Beacon build() throws InvalidParameterException {
            if (this.uuid == null) {
                throw new IllegalArgumentException("Uuid is not set");
            }

            if (this.major < MAJOR_MINOR_MIN_VALUE || this.major > MAJOR_MINOR_MAX_VALUE) {
                throw new IllegalArgumentException("Major should be a number from 0 to 65535.");
            }

            if (this.minor < MAJOR_MINOR_MIN_VALUE || this.minor > MAJOR_MINOR_MAX_VALUE) {
                throw new IllegalArgumentException("Minor should be a number from 0 to 65535.");
            }

            return new Beacon(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.uuid);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.rssi);
        dest.writeInt(this.power);
        dest.writeParcelable(this.device, flags);
        dest.writeTypedList(this.services);
    }

    protected Beacon(Parcel in) {
        this.uuid = (UUID) in.readSerializable();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.rssi = in.readInt();
        this.power = in.readInt();
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.services = in.createTypedArrayList(ParcelUuid.CREATOR);
    }

    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        @Override
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        @Override
        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };
}
