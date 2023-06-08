package com.xannanov.graduatework.presentation.addnewdevice.bt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xannanov.graduatework.R
import com.xannanov.graduatework.databinding.FragmentAddDeviceByBtBinding
import com.xannanov.graduatework.presentation.bt.list.BTListActivity
import com.xannanov.graduatework.presentation.bt.list.BTRecycleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

var i = 0
@AndroidEntryPoint
class AddDeviceByBtFragment : Fragment() {

    private val viewModel by viewModels<AddDeviceByBtViewModel>()

    private val binding by viewBinding(FragmentAddDeviceByBtBinding::bind)
    private val adapter: BTRecycleAdapter by lazy {
        BTRecycleAdapter {
            viewModel.connectToDevice(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_device_by_bt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initViews() {
        with(binding) {
            rvScannedDevices.adapter = adapter
            btnSendInfo.setOnClickListener {
                val message = "${etInputSsidName.text}#${etInputSsidPassword.text}#${etInputMqttLogin.text}#${etInputMqttPassword.text}#${etInputMqttTopic.text}"
                viewModel.sendMessage(message)
            }
        }
    }

    private fun initObservers() {
        viewModel.startScan()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect {
                    Log.i("asdfasdf", "${it.message} ${it.deviceModel}")
                    if (it.message != null) {
                        when (it.message) {
                            is MessageFromIoTDevice.MessageDeviceConnected -> {
                                Toast.makeText(requireActivity(), "Connected", Toast.LENGTH_SHORT).show()
                            }
                            is MessageFromIoTDevice.MessageSettingComplete -> {
                                viewModel.saveDevice(it.message.deviceUUID, it.message.deviceType)
                                viewModel.disconnectFromDevice()
                            }
                            is MessageFromIoTDevice.MessageSettingNextButton -> {
                                with(binding) {
                                    llSettings.isVisible = false
                                    rvScannedDevices.isVisible = false
                                    tvSettingStep.isVisible = true
                                    tvSettingStep.text = "${i++}"
                                }
                            }
                        }
                    }
                    if (it.deviceSaved && it.deviceModel != null) {
                        viewModel.navigateToRootScreen()
                    }
                    if (it.isConnected) {
                        viewModel.stopScan()
                        Toast.makeText(requireActivity(), "Connected", Toast.LENGTH_SHORT).show()
                        binding.rvScannedDevices.visibility = View.GONE
                        binding.llSettings.visibility = View.VISIBLE
                    }
                    adapter.submitList(it.scannedDevices)
                }
            }
        }
    }

    companion object {
    }
}