package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class GeneralSettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsList> = ArrayList()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewGeneralSettings)

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterSettings(settingsList, requireContext()))

        settingsList.clear()

        // Configurações movidas para a tela principal de configurações
        // addToAdapter(R.string.debug_settings_title, R.string.debug_settings_desc, R.drawable.ic_settings_outline)
        // addToAdapter(R.string.sound_settings_title, R.string.sound_settings_desc, R.drawable.ic_sound)
        // addToAdapter(R.string.env_settings_title, R.string.env_settings_desc, R.drawable.ic_globe)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, icon: Int) {
        settingsList.add(
            SettingsList(getString(titleId), getString(descriptionId), icon)
        )
    }
}
