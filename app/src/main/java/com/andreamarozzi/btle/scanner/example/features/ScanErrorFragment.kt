package com.andreamarozzi.btle.scanner.example.features

import android.Manifest
import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.andreamarozzi.btle.scanner.example.R
import com.andreamarozzi.btle.scanner.example.features.shared.BaseScanFragment
import com.marozzi.btle.scanner.model.ScanError
import kotlinx.android.synthetic.main.fragment_scan_error.view.*
import kotlinx.android.synthetic.main.fragment_scan_error.*
import kotlinx.android.synthetic.main.view_scan_error.view.*

class ScanErrorFragment : BaseScanFragment() {

    companion object {

        private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
        private const val REQUEST_CODE_PERMISSION_LOCATION = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.registerForScanError().observe(this, Observer {
            it?.let { errors ->
                view?.scan_error_list?.apply {
                    removeAllViews()
                    if (errors.isEmpty()) {
                        addView(getErrorView(null))
                    } else {
                        errors.forEach { error ->
                            addView(getErrorView(error))
                        }
                    }
                }
            }
        })
    }

    private fun getErrorView(error: ScanError?): View {
        val view = layoutInflater.inflate(R.layout.view_scan_error, scan_error_list, false)
        val stringRes = when (error) {
            ScanError.NO_BLUETOOTH_PERMISSION -> R.string.no_bt_permission
            ScanError.NO_BLUETOOTH_LE -> R.string.no_btle
            ScanError.BLUETOOTH_OFF -> R.string.bt_off
            ScanError.LOCATION_OFF -> R.string.gps_off
            ScanError.NO_LOCATION_PERMISSION -> R.string.no_gps_permission
            ScanError.UNKNOWN -> R.string.unknow_bt_error
            ScanError.PROVIDER_BEACON_ERROR -> R.string.bt_provider_error
            else -> R.string.no_error
        }
        view.scan_error_title.setText(stringRes)
        view.setOnClickListener {
            when (error) {
                ScanError.NO_BLUETOOTH_PERMISSION -> Toast.makeText(context, R.string.change_your_manifest, Toast.LENGTH_SHORT).show()
                ScanError.NO_BLUETOOTH_LE -> Toast.makeText(context, R.string.nothing_todo, Toast.LENGTH_SHORT).show()
                ScanError.BLUETOOTH_OFF -> BluetoothAdapter.getDefaultAdapter().enable()
                ScanError.LOCATION_OFF -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                ScanError.NO_LOCATION_PERMISSION -> requestPermissions(permissions, REQUEST_CODE_PERMISSION_LOCATION)
                null -> {
                    //nothing to do here
                }
                else -> Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan_error, container, false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.checkIfCanScan()
    }
}