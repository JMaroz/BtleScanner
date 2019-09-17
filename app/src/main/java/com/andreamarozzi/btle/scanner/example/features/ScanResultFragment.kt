package com.andreamarozzi.btle.scanner.example.features

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.andreamarozzi.btle.scanner.example.R
import android.view.ViewGroup
import com.andreamarozzi.btle.scanner.example.features.shared.BaseScanFragment
import com.andreamarozzi.btle.scanner.example.features.shared.BtDeviceItem
import com.marozzi.btle.scanner.interfaces.iBeacon
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import kotlinx.android.synthetic.main.fragment_scan_result.view.*
import java.util.concurrent.ConcurrentHashMap

class ScanResultFragment : BaseScanFragment() {

    private val handler = Handler()
    private val runnable = Runnable {
        updateScanResult()
    }

    private val map : ConcurrentHashMap<String, iBeacon> = ConcurrentHashMap()
    private val observer = Observer<iBeacon> { it ->
        it?.let {
            Log.d("ScanResultFragment", "new $it")
            synchronized(map) {
                map[it.device.address] = it
            }
        }
    }

    private val fastAdapter = FastItemAdapter<BtDeviceItem>()

    private fun updateScanResult() {
        synchronized(map) {
            val list: MutableList<BtDeviceItem> = mutableListOf()
            map.values.sortedBy { it.rssi }.mapTo(list) {
                BtDeviceItem.getBtDeviceItem(it)
            }
            FastAdapterDiffUtil.set(fastAdapter, list)
            //fastAdapter.clear()
            //fastAdapter.add(list)
            handler.postDelayed(runnable, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.registerForScanResult().observe(this, observer)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scan_result, container, false)
        view.recycler_view.layoutManager = LinearLayoutManager(context!!)
        view.recycler_view.adapter = fastAdapter
        view.recycler_view.itemAnimator?.apply {
            addDuration = 500
            removeDuration = 500
            moveDuration = 500
            changeDuration = 500
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}