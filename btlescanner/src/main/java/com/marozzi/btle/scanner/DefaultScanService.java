package com.marozzi.btle.scanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.marozzi.btle.scanner.interfaces.BluetoothFactory;
import com.marozzi.btle.scanner.interfaces.Provider;
import com.marozzi.btle.scanner.interfaces.ScanService;
import com.marozzi.btle.scanner.interfaces.ScanServiceCallback;
import com.marozzi.btle.scanner.interfaces.iBeacon;
import com.marozzi.btle.scanner.model.Beacon;
import com.marozzi.btle.scanner.model.ScanError;
import com.marozzi.btle.scanner.model.ScanState;
import com.marozzi.btle.scanner.utils.BluetoothUtils;
import com.marozzi.btle.scanner.utils.LocationUtils;
import com.marozzi.btle.scanner.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link ScanService} will monitor for the {@link Beacon}s passed via {@link #addBeaconToScan(Beacon)}.
 * notify you when you are in range of a monitored {@link Beacon}.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class DefaultScanService implements ScanService, Provider.ProviderCallback<iBeacon> {

    private final Context context;
    private final BluetoothFactory bluetoothFactory;
    private final Set<Beacon> beaconsFilter;

    private Provider<iBeacon> provider;
    private Object scanCallback;
    private ScanServiceCallback callback;
    private ScanState scanState = ScanState.STATE_STOPPED;

    public DefaultScanService(@NonNull Context context, @NonNull BluetoothFactory bluetoothFactory, @NonNull Provider provider) {
        this.context = context.getApplicationContext();
        this.bluetoothFactory = bluetoothFactory;
        this.beaconsFilter = new HashSet<>();

        setProvider(provider);
    }

    //region ScanService

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void addBeaconToScan(@NonNull final Beacon beacon) {
        beaconsFilter.add(beacon);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void removeBeaconToScan(@NonNull final Beacon beacon) {
        beaconsFilter.remove(beacon);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    @Override
    @NonNull
    public List<ScanError> canScan() {
        List<ScanError> errors = new ArrayList<>();
        if (!bluetoothFactory.canAttachBluetoothAdapter())
            errors.add(ScanError.NO_BLUETOOTH_PERMISSION);

        if (!BluetoothUtils.hasBluetoothLE(context))
            errors.add(ScanError.NO_BLUETOOTH_LE);

        if (!BluetoothUtils.isBluetoothOn())
            errors.add(ScanError.BLUETOOTH_OFF);

        if (!LocationUtils.isLocationOn(context))
            errors.add(ScanError.LOCATION_OFF);

        if (!PermissionUtils.isLocationGranted(context))
            errors.add(ScanError.NO_LOCATION_PERMISSION);

        if (bluetoothFactory.getBluetoothLeScanner() == null)
            errors.add(ScanError.UNKNOWN);

        return errors;
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    @Override
    public void start() {
        stopScan();

        boolean canScan = true;
        if (!bluetoothFactory.canAttachBluetoothAdapter()) {
            canScan = false;

            if (callback != null) {
                callback.onError(ScanError.NO_BLUETOOTH_PERMISSION);
            }
        } else {
            if (!BluetoothUtils.hasBluetoothLE(context)) {
                canScan = false;

                if (callback != null) {
                    callback.onError(ScanError.NO_BLUETOOTH_LE);
                }
            }

            if (!BluetoothUtils.isBluetoothOn()) {
                canScan = false;

                if (callback != null) {
                    callback.onError(ScanError.BLUETOOTH_OFF);
                }
            }
        }

        if (!LocationUtils.isLocationOn(context)) {
            canScan = false;

            if (callback != null) {
                callback.onError(ScanError.LOCATION_OFF);
            }
        }

        if (!PermissionUtils.isLocationGranted(context)) {
            canScan = false;

            if (callback != null) {
                callback.onError(ScanError.NO_LOCATION_PERMISSION);
            }
        }

        if (canScan) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothLeScanner scanner = bluetoothFactory.getBluetoothLeScanner();
                if (scanner == null) {
                    scanState = ScanState.STATE_ERROR;
                    if (callback != null) {
                        callback.onError(ScanError.UNKNOWN);
                    }
                    return;
                }
                List<ScanFilter> scanFilters = new ArrayList<>();
                if (!beaconsFilter.isEmpty()) {
                    for (final Beacon beacon : beaconsFilter) {
                        if (beacon != null)
                            scanFilters.add(beacon.toScanFilter());
                    }
                }

                if (scanCallback == null)
                    scanCallback = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            ScanRecord record = result.getScanRecord();
                            byte[] data = record != null ? record.getBytes() : new byte[0];
                            List<ParcelUuid> services = record != null ? record.getServiceUuids() : null;

                            if (provider != null)
                                provider.elaborateBeacon(BluetoothUtils.parse(result.getDevice(), result.getRssi(), data, services));
                        }
                    };
                scanner.startScan(scanFilters, getScanSettings(), (ScanCallback) scanCallback);
            } else {
                BluetoothAdapter adapter = bluetoothFactory.getBluetoothAdapter();
                if (adapter == null) {
                    scanState = ScanState.STATE_ERROR;
                    if (callback != null) {
                        callback.onError(ScanError.UNKNOWN);
                    }
                    return;
                }

                if (scanCallback == null)
                    scanCallback = new BluetoothAdapter.LeScanCallback() {

                        @Override
                        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                            if (provider != null)
                                provider.elaborateBeacon((BluetoothUtils.parse(device, rssi, scanRecord, BluetoothUtils.getServicesFromBytes(scanRecord))));
                        }
                    };
                adapter.startLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
            }
            provider.start();
            scanState = ScanState.STATE_SCANNING;
        } else {
            scanState = ScanState.STATE_ERROR;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void stopScan() {
        try {//https://issuetracker.google.com/issues/37032956
            if (scanCallback != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (bluetoothFactory.getBluetoothLeScanner() != null)
                        bluetoothFactory.getBluetoothLeScanner().stopScan((ScanCallback) scanCallback);
                } else {
                    if (bluetoothFactory.getBluetoothAdapter() != null)
                        bluetoothFactory.getBluetoothAdapter().stopLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanState = ScanState.STATE_STOPPED;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    @Override
    public void stop() {
        stopScan();
        provider.stop();
    }

    @Override
    public void setCallback(@NonNull final ScanServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setProvider(@NonNull Provider<iBeacon> provider) {
        this.provider = provider;
        this.provider.setProviderCallback(this);
    }

    @NonNull
    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public ScanState getState() {
        return scanState;
    }

    @Override
    public void onProviderCompleted(@NonNull iBeacon result) {
        if (callback != null)
            callback.onBeaconFound(result);
    }

    @Override
    public void onProviderError(@NonNull iBeacon beacon) {
        if (callback != null)
            callback.onError(ScanError.PROVIDER_BEACON_ERROR);
    }

    //endregion

    //region Helpers

    private ScanSettings getScanSettings() {
        final ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }

    //endregion
}
