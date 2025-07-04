package com.micewine.emu.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class RatPackagesSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rat_packages_settings)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRatPackagesSettings)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.rat_manager_title), getString(R.string.rat_manager_desc), R.drawable.ic_rat_package_grayscale))
        settingsList.add(SettingsList(getString(R.string.rat_downloader_title), getString(R.string.rat_downloader_desc), R.drawable.ic_download))

        recyclerView.adapter = AdapterSettings(settingsList, this@RatPackagesSettingsActivity)
    }
} 