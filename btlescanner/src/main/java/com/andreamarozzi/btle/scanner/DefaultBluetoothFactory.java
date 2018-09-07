package com.andreamarozzi.btle.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;
import android.support.annotation.Nullable;

import com.andreamarozzi.btle.scanner.interfaces.BluetoothFactory;

/**
 * Created by eliaslecomte on 12/12/2016.
 */

public class DefaultBluetoothFactory implements BluetoothFactory {

    private BluetoothAdapter bluetoothAdapter;

    /**
     * Attaches the {@link #bluetoothAdapter} if it is null.
     *
     * @return true if the {@link BluetoothAdapter} and {@link BluetoothLeScanner} are available
     */
    @Override
    public boolean canAttachBluetoothAdapter() {
        // try to get the BluetoothAdapter
        // apps running in a Samsung Knox container will crash with a SecurityException
        if (bluetoothAdapter == null) {
            try {
                bluetoothAdapter = getBluetoothAdapter();
            } catch (Exception exception) {
                return false;
            }
        }

        // try to get the BluetoothLeScanner
        // apps without Bluetooth permission will crash with a SecurityException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                return bluetoothAdapter.getBluetoothLeScanner() != null;
            } catch (Exception exception) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return the {@link BluetoothLeScanner}
     */
    @Override
    @Nullable
    public BluetoothLeScanner getBluetoothLeScanner() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && canAttachBluetoothAdapter() ? bluetoothAdapter.getBluetoothLeScanner() : null;
    }

    /**
     * @return {@link BluetoothAdapter#getDefaultAdapter()}
     */
    @Override
    @Nullable
    public BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }
}
