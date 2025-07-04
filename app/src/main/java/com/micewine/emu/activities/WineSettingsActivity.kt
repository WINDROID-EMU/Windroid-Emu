package com.micewine.emu.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.micewine.emu.R
import com.micewine.emu.fragments.WineSettingsFragment

class WineSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wine_settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.wineSettingsContent, WineSettingsFragment())
                .commit()
        }
    }
} 