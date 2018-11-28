package com.andreamarozzi.btle.scanner.example.features.shared

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import com.andreamarozzi.btle.scanner.example.features.MainActivityViewModel

open class BaseScanFragment : Fragment() {

    protected lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainActivityViewModel::class.java)
            viewModel.init(it)
        }
    }
}