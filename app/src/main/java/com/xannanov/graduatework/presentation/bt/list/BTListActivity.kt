package com.xannanov.graduatework.presentation.bt.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xannanov.graduatework.R
import com.xannanov.graduatework.databinding.ActivityBtactivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BTListActivity : AppCompatActivity() {

    private val viewModel by viewModels<BTListViewModel>()

    private val adapter by lazy {
        BTRecycleAdapter {
            viewModel.connectToDevice(it)
//            setResult(
//                RESULT_OK,
//                Intent().apply {
//                    putExtra(DEVICE_KEY, it)
//                }
//            )
//            finish()
        }
    }

    private val binding by viewBinding(ActivityBtactivityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btactivity)

        viewModel.startScan()
        initViews()
    }

    private fun initViews() {
        with(binding) {
            rvBt.adapter = adapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect {
                    if (it.isConnected) {
                        Log.i("asdfasdf", "send password")
                        viewModel.sendMessage(DEFAULT_MESSAGE)
                    }
                    adapter.submitList(it.scannedDevices + it.pairedDevices)
                }
            }
        }
    }

    companion object {
        const val DEVICE_KEY = "device_key"
        const val DEFAULT_MESSAGE = "Tattelecom_44D9#03112000#verzzil#mueoq0gm#/user/verzzil/iot"
    }
}