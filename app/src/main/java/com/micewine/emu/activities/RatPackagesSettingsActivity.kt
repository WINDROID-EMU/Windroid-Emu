package com.micewine.emu.activities

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class RatPackagesSettingsActivity : AppCompatActivity() {
    private var backButton: ImageButton? = null
    private var ratPackagesSettingsToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rat_packages_settings)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRatPackagesSettings)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.rat_manager_title), getString(R.string.rat_manager_desc), R.drawable.ic_rat_package_grayscale))
        settingsList.add(SettingsList(getString(R.string.rat_downloader_title), getString(R.string.rat_downloader_desc), R.drawable.ic_download))

        recyclerView.adapter = AdapterSettings(settingsList, this@RatPackagesSettingsActivity)

        // Configurar toolbar e botão de voltar
        ratPackagesSettingsToolbar = findViewById(R.id.ratPackagesSettingsToolbar)
        ratPackagesSettingsToolbar?.title = getString(R.string.rat_packages_settings_title)

        backButton = findViewById(R.id.backButton)
        backButton?.setOnClickListener {
            onKeyDown(KeyEvent.KEYCODE_BACK, null)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return true
    }
} 