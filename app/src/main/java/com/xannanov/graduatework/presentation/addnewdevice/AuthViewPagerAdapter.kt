package com.xannanov.graduatework.presentation.addnewdevice

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.xannanov.graduatework.presentation.addnewdevice.bt.AddDeviceByBtFragment
import com.xannanov.graduatework.presentation.addnewdevice.mqtt.AddDeviceByMqttFragment

class AuthViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            AddDeviceByBtFragment()
        } else {
            AddDeviceByMqttFragment()
        }
    }
}