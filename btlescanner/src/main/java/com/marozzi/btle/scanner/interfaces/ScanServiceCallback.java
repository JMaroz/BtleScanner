package com.marozzi.btle.scanner.interfaces;

import android.support.annotation.NonNull;

import com.marozzi.btle.scanner.model.ScanError;

/**
 * Created by amarozzi on 16/02/18.
 */

public interface ScanServiceCallback {

    /**
     * The ScanService found a beacon that match the requested criteria
     *
     * @param beacon device found
     */
    void onBeaconFound(@NonNull iBeacon beacon);

    /**
     * Something went wrong, check {@code error}.
     *
     * @param scanError that happened
     */
    void onError(@NonNull ScanError scanError);

}