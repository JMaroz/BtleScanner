package com.andreamarozzi.btle.scanner.example.features

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andreamarozzi.btle.scanner.example.R
import com.andreamarozzi.btle.scanner.example.features.shared.BaseScanFragment
import kotlinx.android.synthetic.main.fragment_scan_controller.view.*

class ScanControllerFragment : BaseScanFragment() {

    //pulsanti per star scan, stop scan, check if can scan, clear scan list

    private var scanning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scan_controller, container, false)

        view.check_error.setOnClickListener {
            viewModel.checkIfCanScan()
        }

        view.start_scan.setOnClickListener {
            if (scanning) {
                viewModel.stopScan()
                view.start_scan.setText(R.string.start_scan)
            } else {
                viewModel.startScan()
                view.start_scan.setText(R.string.stop_scan)
            }
            scanning = !scanning
        }

        return view
    }
}