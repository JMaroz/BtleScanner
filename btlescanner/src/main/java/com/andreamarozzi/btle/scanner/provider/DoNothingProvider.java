package com.andreamarozzi.btle.scanner.provider;

import android.support.annotation.NonNull;

import com.andreamarozzi.btle.scanner.interfaces.Provider;
import com.andreamarozzi.btle.scanner.interfaces.iBeacon;

/**
 * Created by amarozzi on 15/02/18.
 */

public class DoNothingProvider implements Provider<iBeacon> {

    private ProviderCallback<iBeacon> callback;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setProviderCallback(@NonNull ProviderCallback<iBeacon> callback) {
        this.callback = callback;
    }

    @Override
    public void elaborateBeacon(@NonNull iBeacon beacon) {
        callback.onProviderCompleted(beacon);
    }
}
