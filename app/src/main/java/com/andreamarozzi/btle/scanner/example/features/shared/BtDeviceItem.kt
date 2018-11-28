package com.andreamarozzi.btle.scanner.example.features.shared

import android.view.View
import com.andreamarozzi.btle.scanner.interfaces.iBeacon
import com.mikepenz.fastadapter.items.AbstractItem
import com.andreamarozzi.btle.scanner.example.R
import com.mikepenz.fastadapter.FastAdapter
import kotlinx.android.synthetic.main.item_bt_device.view.*

class BtDeviceItem private constructor(val device: iBeacon) : AbstractItem<BtDeviceItem, BtDeviceItem.ViewHolder>() {

    companion object {
        fun getBtDeviceItem(device: iBeacon): BtDeviceItem {
            return BtDeviceItem(device).withIdentifier(device.device.address.hashCode().toLong())
        }
    }

    override fun getType(): Int {
        return R.id.fastadapter_bt_device_item_id
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun getLayoutRes(): Int {
        return R.layout.item_bt_device
    }

    /**
     * our ViewHolder
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<BtDeviceItem>(view) {
        override fun unbindView(item: BtDeviceItem) {
            itemView.mac_address.text = item.device.device.address
        }

        override fun bindView(item: BtDeviceItem, payloads: MutableList<Any>) {
        }
    }
}