package com.marozzi.btle.scanner.provider;

import android.support.annotation.NonNull;

import com.marozzi.btle.scanner.interfaces.Provider;
import com.marozzi.btle.scanner.interfaces.iBeacon;

/**
 * This is a Default Provider, will not check any beacon found but will call {@link com.marozzi.btle.scanner.interfaces.Provider.ProviderCallback#onProviderCompleted(iBeacon)}
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
