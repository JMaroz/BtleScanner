package com.andreamarozzi.btle.scanner.interfaces;

import android.support.annotation.NonNull;

import com.andreamarozzi.btle.scanner.model.Beacon;
import com.andreamarozzi.btle.scanner.model.ScanError;
import com.andreamarozzi.btle.scanner.model.ScanState;

import java.util.List;

/**
 * Interface {@link ScanService} specifies the contract for a {@link Beacon} scanner.
 * Created by amarozzi on 14/02/2018.
 */

public interface ScanService {

    /**
     * Add {@code beacon} to scan and all previously added {@link Beacon}s.
     *
     * @param beacon to find
     */
    void addBeaconToScan(@NonNull Beacon beacon);

    /**
     * Remove for {@code beacon}. Keep scanning for other {@link Beacon}s that were added with
     * {@link #addBeaconToScan(Beacon)}.
     *
     * @param beacon to remove
     */
    void removeBeaconToScan(@NonNull Beacon beacon);

    /**
     * Check if the service can permof scan for beacons
     * @return the this of errors like {@link ScanError#NO_BLUETOOTH_PERMISSION} or empty list if there are no erros
     */
    @NonNull
    List<ScanError> canScan();

    /**
     * Starts scanning and provider.
     */
    void start();

    /**
     * This stop the scan, is similar to {@link ScanService#stop()} but stop only the Bluetooth and not the provider
     */
    void stopScan();

    /**
     * Stops scanning and provider.
     */
    void stop();

    /**
     * Set the {@link ScanServiceCallback} that will be notified for {@link iBeacon} scannder or if an {@link ScanError}
     * happened.
     *
     * @param callback that will be notified when something happens
     */
    void setCallback(@NonNull ScanServiceCallback callback);

    /**
     * Set the {@link Provider}. A Provider receive the {@link iBeacon} scanned and do something, if the provider return {@link Provider.ProviderCallback#elaborateBeacon(iBeacon)} the ScanService call {@link ScanServiceCallback#onBeaconFound(iBeacon)}
     *
     * @param provider
     */
    void setProvider(@NonNull Provider provider);

    @NonNull
    Provider getProvider();

    /**
     * Return the state of the ScanService
     *
     * @return
     */
    ScanState getState();
}