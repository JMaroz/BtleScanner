package com.andreamarozzi.btle.scanner.interfaces;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import java.util.List;
import java.util.UUID;

/**
 * Created by amarozzi on 14/02/2018.
 */

public interface iBeacon {

    UUID getUUID();

    int getMajor();

    int getMinor();

    int getRssi();

    int getPower();

    BluetoothDevice getDevice();

    List<ParcelUuid> getServices();
}
