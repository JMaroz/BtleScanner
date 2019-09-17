package com.andreamarozzi.btle.scanner.example.features

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.andreamarozzi.btle.scanner.example.features.shared.BtleScannerLiveData
import com.marozzi.btle.scanner.interfaces.iBeacon
import com.marozzi.btle.scanner.model.ScanError

class MainActivityViewModel : ViewModel() {

    private var scanner: BtleScannerLiveData? = null
    private val scanErrorData: MutableLiveData<List<ScanError>> = MutableLiveData()

    fun init(context: Context) {
        if (scanner == null)
            scanner = BtleScannerLiveData(context)
    }

    override fun onCleared() {
        scanner?.stopScan()
        scanner = null
    }

    fun registerForScanError(): MutableLiveData<List<ScanError>> {
        return scanErrorData
    }

    fun registerForScanResult(): LiveData<iBeacon> = scanner!!

    fun checkIfCanScan() {
        scanErrorData.value = scanner?.canScan()
    }

    fun startScan() {
        scanner?.startScan()
    }

    fun stopScan() {
        scanner?.stopScan()
    }

}