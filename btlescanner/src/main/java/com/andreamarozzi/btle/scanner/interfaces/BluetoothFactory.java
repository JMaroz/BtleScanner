package com.andreamarozzi.btle.scanner.interfaces;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.support.annotation.Nullable;

/**
 * BluetoothFactory is responsible for managing the {@link BluetoothLeScanner} used.
 * Created by amarozzi on 14/02/2018.
 */

public interface BluetoothFactory {
    /**
     * Attaches the {@link BluetoothAdapter} if it is null.
     *
     * @return true if the {@link BluetoothAdapter} and {@link BluetoothLeScanner} are available
     */
    boolean canAttachBluetoothAdapter();

    /**
     * @return a {@link BluetoothLeScanner}
     */
    @Nullable
    BluetoothLeScanner getBluetoothLeScanner();

    /**
     * @return {@link BluetoothAdapter#getDefaultAdapter()}
     */
    BluetoothAdapter getBluetoothAdapter();

}