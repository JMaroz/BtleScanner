package com.andreamarozzi.btle.scanner.example.features.shared

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.marozzi.btle.scanner.DefaultBluetoothFactory
import com.marozzi.btle.scanner.DefaultScanService
import com.marozzi.btle.scanner.interfaces.Provider
import com.marozzi.btle.scanner.interfaces.Provider.ProviderCallback
import com.marozzi.btle.scanner.interfaces.ScanService
import com.marozzi.btle.scanner.interfaces.ScanServiceCallback
import com.marozzi.btle.scanner.interfaces.iBeacon
import com.marozzi.btle.scanner.model.Beacon
import com.marozzi.btle.scanner.model.ScanError

class BtleScannerLiveData(context: Context) : LiveData<iBeacon>() {

    companion object {
        const val TAG = "BtleScannerLiveData"
    }

    /**
     * The actual scanner for bt
     */
    private var scanService: ScanService

    /**
     * The callback when a new beacon is found
     */
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
        //startScan()
    }

    override fun onInactive() {
        super.onInactive()
        stopScan()
    }

    fun startScan() {
        scanService.start()
    }

    fun stopScan() {
        scanService.stop()
    }

    fun setProvider(provider: Provider<iBeacon>) {
        scanService.stop()
        scanService.provider = provider
        scanService.start()
    }

    fun addBeaconToScan(beacon: Beacon) {
        scanService.stop()
        scanService.addBeaconToScan(beacon)
        scanService.start()
    }

    fun removeBeaconToScan(beacon: Beacon) {
        scanService.stop()
        scanService.removeBeaconToScan(beacon)
        scanService.start()
    }

    fun canScan(): List<ScanError> = scanService.canScan()

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