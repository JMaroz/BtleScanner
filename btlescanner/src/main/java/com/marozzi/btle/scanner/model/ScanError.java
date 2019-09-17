package com.marozzi.btle.scanner.model;

/**
 * Created by amarozzi on 14/02/2018.
 */

public enum ScanError {
    /**
     * Unable to get BluetoothAdapter, this is very uncommon error but happen when in the manifest is missing Bluetooth Permission
     */
    NO_BLUETOOTH_PERMISSION,
    /**
     * The device not support the scan for bluetooth low energy
     */
    NO_BLUETOOTH_LE,
    BLUETOOTH_OFF,
    LOCATION_OFF,
    NO_LOCATION_PERMISSION,
    UNKNOWN,
    PROVIDER_BEACON_ERROR
}
