package com.andreamarozzi.btle.scanner.interfaces;

import android.support.annotation.NonNull;

/**
 * Created by amarozzi on 15/02/18.
 * A provider must elaborate the iBeacon and report via ProviderCallback the response
 */

public interface Provider<T extends iBeacon> {

    /**
     * Called from a Service
     */
    void start();

    /**
     * Called from a Service
     */
    void stop();

    /**
     * @param callback a callback to provide a response
     */
    void setProviderCallback(@NonNull ProviderCallback<T> callback);

    /**
     * Elaborate the beacon
     *
     * @param beacon the beacon
     */
    void elaborateBeacon(@NonNull iBeacon beacon);

    interface ProviderCallback<T extends iBeacon> {

        /**
         * When the provider as elaborated the beacon and the work is completed
         *
         * @param result
         */
        void onProviderCompleted(@NonNull T result);

        /**
         * There was an error while elaborating the beacon
         *
         * @param beacon the beacon that went wrong
         */
        void onProviderError(@NonNull iBeacon beacon);
    }
}
