package com.micewine.emu.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class DriversSettingsGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers_settings_group)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDriversSettingsGroup)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.driver_settings_title), getString(R.string.driver_settings_desc), R.drawable.ic_gpu))
        settingsList.add(SettingsList(getString(R.string.driver_info_title), getString(R.string.driver_info_desc), R.drawable.ic_gpu))

        recyclerView.adapter = AdapterSettings(settingsList, this@DriversSettingsGroupActivity)
    }
} 