package com.xannanov.graduatework.presentation.start

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.xannanov.graduatework.R
import com.xannanov.graduatework.databinding.FragmentListOfDevicesBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xannanov.graduatework.presentation.navigation.Navigator
import com.xannanov.graduatework.presentation.addnewdevice.bt.AddDeviceByBtViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListOfDevicesFragment : Fragment() {

    private val viewModel by viewModels<ListOfDevicesViewModel>()

    private val binding by viewBinding(FragmentListOfDevicesBinding::bind)
    private val adapter by lazy {
        DeviceListAdapter {
            Navigator.navigateToManageDevice(it.uuid)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_of_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllDevices()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect {
                    adapter.submitList(it.devices)
                }
            }
        }
    }

    private fun initViews() {
        with(binding) {
            rvDevices.adapter = adapter
            fabAddNewDevice.setOnClickListener {
                Navigator.navigate(R.id.action_listOfDevicesFragment_to_addNewDeviceFragment)
                Toast.makeText(requireActivity(), "clicked", Toast.LENGTH_SHORT).show()
                viewModel.getAllDevices()
            }
        }
    }

    companion object {

    }
}