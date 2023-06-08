package com.xannanov.graduatework.presentation.bt.contorl

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xannanov.graduatework.R
import com.xannanov.graduatework.databinding.ActivityBtactivityBinding
import com.xannanov.graduatework.databinding.ActivityContorlBinding
import com.xannanov.graduatework.domain.bt.BtConnection
import com.xannanov.graduatework.presentation.bt.list.BTListActivity
import com.xannanov.graduatework.presentation.bt.list.BTListActivity.Companion.DEVICE_KEY
import com.xannanov.graduatework.domain.bt.model.BTModel

class ControlActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityContorlBinding::bind)
    private lateinit var actListLauncher: ActivityResultLauncher<Intent>
    private val btConnection by lazy {
        BtConnection((getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter)
    }
    private lateinit var btModel: BTModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contorl)

        onBtnResult()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_control, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.i_connect -> {
                btConnection.connect(btModel.mac)
            }
            R.id.i_list -> {
                actListLauncher.launch(Intent(this, BTListActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBtnResult() {
        actListLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                btModel = it.data?.getSerializableExtra(DEVICE_KEY) as BTModel
            }
        }
    }
}