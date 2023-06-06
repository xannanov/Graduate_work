package com.xannanov.graduatework.presentation.addnewdevice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.xannanov.graduatework.R
import com.xannanov.graduatework.databinding.FragmentAddNewDeviceBinding
import com.xannanov.graduatework.databinding.FragmentListOfDevicesBinding

class AddNewDeviceFragment : Fragment() {

    private val binding by viewBinding(FragmentAddNewDeviceBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_new_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
    }

    private fun initUi() {
        initPagerAdapter()

        initTabLayoutMediator()
    }

    private fun initTabLayoutMediator() {
        TabLayoutMediator(binding.authTabLayout, binding.authViewPager) { tab, position ->
            if (position == 0) {
                tab.text = "Bluetooth"
            } else {
                tab.text = "MQTT"
            }
        }.attach()
    }

    private fun initPagerAdapter() {
        binding.authViewPager.adapter = AuthViewPagerAdapter(this)
    }

    companion object {
    }
}