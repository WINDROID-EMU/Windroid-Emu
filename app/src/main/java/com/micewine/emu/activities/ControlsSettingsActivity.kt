package com.micewine.emu.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class ControlsSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controls_settings)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewControlsSettings)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.controller_mapper_title), getString(R.string.controller_mapper_desc), R.drawable.ic_joystick))
        settingsList.add(SettingsList(getString(R.string.virtual_controller_mapper_title), getString(R.string.controller_virtual_mapper_desc), R.drawable.ic_joystick))
        settingsList.add(SettingsList(getString(R.string.controller_view_title), getString(R.string.controller_view_desc), R.drawable.ic_joystick))

        recyclerView.adapter = AdapterSettings(settingsList, this@ControlsSettingsActivity)
    }

    override fun onResume() {
        super.onResume()
        // O clique dos itens é tratado no AdapterSettings, que já abre as telas corretas pelo título
    }
} 