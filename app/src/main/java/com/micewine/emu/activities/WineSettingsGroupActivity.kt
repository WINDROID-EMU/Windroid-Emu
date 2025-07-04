package com.micewine.emu.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class WineSettingsGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wine_settings_group)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewWineSettingsGroup)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.wine_prefix_manager_title), getString(R.string.wine_prefix_manager_desc), R.drawable.ic_wine))
        settingsList.add(SettingsList(getString(R.string.wine_settings_title), getString(R.string.wine_settings_desc), R.drawable.ic_wine))

        recyclerView.adapter = AdapterSettings(settingsList, this@WineSettingsGroupActivity)
    }
} 