package com.xannanov.graduatework.presentation.addnewdevice.bt.setting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xannanov.graduatework.R
import com.xannanov.graduatework.databinding.FragmentSettingSmartControlBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingSmartControlFragment : Fragment() {

    @Inject lateinit var factory: SettingSmartControlViewModel.Factory

    private val viewModel: SettingSmartControlViewModel by viewModels {
        SettingSmartControlViewModel.provideSettingSmartControlViewModelFactory(factory, uuid)
    }

    private val args by navArgs<SettingSmartControlFragmentArgs>()
    private val uuid by lazy { args.uuid }
    private val binding by viewBinding(FragmentSettingSmartControlBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting_smart_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect {
                    if (it.isReady) {
                        Log.i("asdfasdf", "Устройство настроено")
                    }
                }
            }
        }
    }

    companion object {
    }
}