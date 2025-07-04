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

class WineSettingsGroupActivity : AppCompatActivity() {
    private var backButton: ImageButton? = null
    private var wineSettingsGroupToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wine_settings_group)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewWineSettingsGroup)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.wine_settings_title), getString(R.string.wine_settings_desc), R.drawable.ic_wine))
        settingsList.add(SettingsList(getString(R.string.wine_prefix_manager_title), getString(R.string.wine_prefix_manager_desc), R.drawable.ic_wine))

        recyclerView.adapter = AdapterSettings(settingsList, this@WineSettingsGroupActivity)

        // Configurar toolbar e botão de voltar
        wineSettingsGroupToolbar = findViewById(R.id.wineSettingsGroupToolbar)
        wineSettingsGroupToolbar?.title = getString(R.string.wine_settings_group_title)

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