package com.xannanov.graduatework.presentation.navigation

import androidx.navigation.NavController
import com.xannanov.graduatework.R
import com.xannanov.graduatework.presentation.addnewdevice.AddNewDeviceFragmentDirections
import com.xannanov.graduatework.presentation.start.ListOfDevicesFragmentDirections

object Navigator {
    private var navController: NavController? = null

    fun attachNavController(navController: NavController) {
        Navigator.navController = navController
    }

    fun navigate(destination: Int) {
        navController?.navigate(destination)
    }

    fun navigateToRoot() {
        navController?.popBackStack(R.id.listOfDevicesFragment, false)
    }

    fun navigateToManageDevice(uuid: String) {
        navController?.navigate(
            ListOfDevicesFragmentDirections.actionListOfDevicesFragmentToManageDeviceFragment(
                uuid
            )
        )
    }

    fun navigateToSettingControl(uuid: String) {
        navController?.navigate(
            AddNewDeviceFragmentDirections.actionAddNewDeviceFragmentToSettingSmartControlFragment(
                uuid
            )
        )
    }
}