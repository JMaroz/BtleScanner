package com.andreamarozzi.btle.scanner.features

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.andreamarozzi.btle.scanner.R
import com.andreamarozzi.btle.scanner.interfaces.iBeacon
import com.andreamarozzi.btle.scanner.utils.checkPermission

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var viewModel: MainActivityViewModel
    private val observer = Observer<iBeacon> {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        val permissions = checkPermission(permissions)
        if (permissions.isNotEmpty())
            ActivityCompat.requestPermissions(this, permissions, 101)
        else
            startScan()
    }

    private fun startScan() {
        viewModel.scanAll(this).observe(this, observer)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermission(permissions).isEmpty())
            startScan()
    }
}
