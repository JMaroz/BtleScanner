package com.andreamarozzi.btle.scanner.features

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.andreamarozzi.btle.scanner.features.shared.BtleScannerLiveData
import com.andreamarozzi.btle.scanner.interfaces.iBeacon

class MainActivityViewModel : ViewModel() {

    fun scanAll(context: Context) : LiveData<iBeacon> {
        return BtleScannerLiveData(context)
    }

}