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

class ControlsSettingsActivity : AppCompatActivity() {
    private var backButton: ImageButton? = null
    private var controlsSettingsToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controls_settings)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewControlsSettings)
        val settingsList = mutableListOf<SettingsList>()

        settingsList.add(SettingsList(getString(R.string.controller_mapper_title), getString(R.string.controller_mapper_desc), R.drawable.ic_joystick))
        settingsList.add(SettingsList(getString(R.string.virtual_controller_mapper_title), getString(R.string.controller_virtual_mapper_desc), R.drawable.ic_joystick))
        settingsList.add(SettingsList(getString(R.string.controller_view_title), getString(R.string.controller_view_desc), R.drawable.ic_joystick))
        settingsList.add(SettingsList(getString(R.string.xinput_layout_editor_title), getString(R.string.xinput_layout_editor_desc), R.drawable.ic_joystick))

        recyclerView.adapter = AdapterSettings(settingsList, this@ControlsSettingsActivity)

        // Configurar toolbar e botão de voltar
        controlsSettingsToolbar = findViewById(R.id.controlsSettingsToolbar)
        controlsSettingsToolbar?.title = getString(R.string.controls_settings_title)

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

    override fun onResume() {
        super.onResume()
        // O clique dos itens é tratado no AdapterSettings, que já abre as telas corretas pelo título
    }
} 