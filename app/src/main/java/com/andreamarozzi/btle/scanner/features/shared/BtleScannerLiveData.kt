package com.andreamarozzi.btle.scanner.features.shared

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.andreamarozzi.btle.scanner.DefaultBluetoothFactory
import com.andreamarozzi.btle.scanner.DefaultScanService
import com.andreamarozzi.btle.scanner.interfaces.Provider
import com.andreamarozzi.btle.scanner.interfaces.Provider.ProviderCallback
import com.andreamarozzi.btle.scanner.interfaces.ScanService
import com.andreamarozzi.btle.scanner.interfaces.ScanServiceCallback
import com.andreamarozzi.btle.scanner.interfaces.iBeacon
import com.andreamarozzi.btle.scanner.model.ScanError

class BtleScannerLiveData(val context: Context) : LiveData<iBeacon>() {

    companion object {
        const val TAG = "BtleScannerLiveData"
    }

    private var scanService: ScanService
    private val scanServiceCallback = object : ScanServiceCallback {
        override fun onBeaconFound(beacon: iBeacon) {
            Log.d(TAG, "ScanServiceCallback.onBeaconFound $beacon")
            value = beacon
        }

        override fun onError(scanError: ScanError) {
            Log.e(TAG, "ScanServiceCallback.onError ${scanError.name}")
            value = null
        }
    }

    init {
        scanService = DefaultScanService(context, DefaultBluetoothFactory(), PassAllProvider())
        scanService.setCallback(scanServiceCallback)
    }

    override fun onActive() {
        super.onActive()
        scanService.start()
    }

    override fun onInactive() {
        super.onInactive()
        scanService.stop()
    }

    class PassAllProvider : Provider<iBeacon> {

        private var callback: ProviderCallback<iBeacon>? = null

        override fun start() {

        }

        override fun stop() {

        }

        override fun setProviderCallback(callback: ProviderCallback<iBeacon>) {
            this.callback = callback
        }

        override fun elaborateBeacon(beacon: iBeacon) {
            callback?.onProviderCompleted(beacon)
        }

    }
}