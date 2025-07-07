package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.deviceArch
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class SettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsList> = ArrayList()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewSettings)

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterSettings(settingsList, requireContext()))

        settingsList.clear()

        addToAdapter(R.string.debug_settings_title, R.string.debug_settings_desc,
            R.drawable.ic_debug
        )
        addToAdapter(R.string.sound_settings_title, R.string.sound_settings_desc, R.drawable.ic_sound)
        addToAdapter(R.string.env_settings_title, R.string.env_settings_desc, R.drawable.ic_globe)
        addToAdapter(R.string.controls_settings_title, R.string.controls_settings_desc, R.drawable.ic_joystick)
        addToAdapter(R.string.wine_settings_group_title, R.string.wine_settings_group_desc, R.drawable.ic_wine)
        addToAdapter(R.string.drivers_settings_group_title, R.string.drivers_settings_group_desc, R.drawable.ic_gpu)
        if (deviceArch != "x86_64") {
            addToAdapter(R.string.box64_preset_manager_title, R.string.box64_preset_manager_desc, R.drawable.ic_box64)
        }
        addToAdapter(R.string.rat_packages_settings_title, R.string.rat_packages_settings_desc, R.drawable.ic_rat_package_grayscale)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, icon: Int) {
        settingsList.add(
            SettingsList(getString(titleId), getString(descriptionId), icon)
        )
    }
}
