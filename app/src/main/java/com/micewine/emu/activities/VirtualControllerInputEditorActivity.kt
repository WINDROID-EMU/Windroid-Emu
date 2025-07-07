package com.micewine.emu.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.slider.Slider
import com.micewine.emu.R
import com.micewine.emu.views.VirtualControllerInputEditorView

class VirtualControllerInputEditorActivity : AppCompatActivity() {
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var editorView: VirtualControllerInputEditorView? = null
    private var editButton: ImageButton? = null

    companion object {
        private const val TAG = "VirtualControllerEditor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forçar orientação landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Configurar tela cheia - esconder barra de navegação
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Esconder barra de navegação e status bar
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        
        setContentView(R.layout.activity_virtual_controller_input_editor)
        
        // Remover qualquer padding ou margin do sistema
        window.decorView.setPadding(0, 0, 0, 0)

        drawerLayout = findViewById(R.id.virtualControllerEditorDrawerLayout)
        navigationView = findViewById(R.id.virtualControllerEditorNavigationView)
        editorView = findViewById(R.id.editorView)
        editButton = findViewById(R.id.editButton)

        Log.d(TAG, "DrawerLayout: $drawerLayout")
        Log.d(TAG, "NavigationView: $navigationView")

        setupNavigationView()
        setupDrawerLayout()
        setupEditButton()
        
        // Configurar cores do NavigationView programaticamente
        setupNavigationViewColors()
        
        // Configurar listener para manter tela cheia
        setupFullscreenListener()
    }

    private fun setupEditButton() {
        editButton?.setOnClickListener {
            showEditDialog()
        }
    }
    
    private fun setupNavigationViewColors() {
        navigationView?.let { navView ->
            // Criar ColorStateList para forçar cor preta
            val colorStateList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.black, theme))
            
            // Aplicar cores programaticamente
            navView.itemTextColor = colorStateList
            navView.itemIconTintList = colorStateList
        }
    }

    private fun setupFullscreenListener() {
        // Listener para manter tela cheia quando o usuário tocar na tela
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // Se a barra de navegação aparecer, esconder novamente
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }
    }

    private fun showEditDialog() {
        // Verificar se há um controle selecionado
        val selectedControl = editorView?.getSelectedControl()
        if (selectedControl == null) {
            Toast.makeText(this, "Selecione um controle primeiro!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_controls, null)
        
        val opacitySlider = dialogView.findViewById<Slider>(R.id.opacitySlider)
        val sizeSlider = dialogView.findViewById<Slider>(R.id.sizeSlider)

        // Definir limites dos sliders ANTES de setar o valor
        opacitySlider.valueFrom = 0f
        opacitySlider.valueTo = 255f
        opacitySlider.stepSize = 1f

        sizeSlider.valueFrom = 50f
        sizeSlider.valueTo = 400f
        sizeSlider.stepSize = 1f

        // Configurar com valores do controle selecionado
        opacitySlider.value = selectedControl.alpha.toFloat()
        sizeSlider.value = selectedControl.radius

        AlertDialog.Builder(this)
            .setTitle("Editar Controle")
            .setView(dialogView)
            .setPositiveButton("Aplicar") { _, _ ->
                val newOpacity = opacitySlider.value.toInt()
                val newSize = sizeSlider.value
                
                // Aplicar apenas ao controle selecionado
                selectedControl.alpha = newOpacity
                selectedControl.radius = newSize
                
                editorView?.invalidate()
                Toast.makeText(this, "Controle atualizado!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupNavigationView() {
        navigationView?.setNavigationItemSelectedListener { menuItem ->
            Log.d(TAG, "Menu item clicked: ${menuItem.title}")
            when (menuItem.itemId) {
                R.id.exitEditor -> {
                    finish()
                    true
                }
                R.id.saveLayout -> {
                    saveLayout()
                    true
                }
                R.id.resetLayout -> {
                    resetLayout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDrawerLayout() {
        // Permitir abrir o drawer deslizando da borda esquerda
        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        
        // Adicionar listener para debug
        drawerLayout?.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: android.view.View) {
                Log.d(TAG, "Drawer opened")
            }
            override fun onDrawerClosed(drawerView: android.view.View) {
                Log.d(TAG, "Drawer closed")
            }
            override fun onDrawerStateChanged(newState: Int) {
                Log.d(TAG, "Drawer state changed: $newState")
            }
        })
    }

    private fun saveLayout() {
        try {
            editorView?.saveCurrentLayout()
            Toast.makeText(this, "Layout salvo com sucesso!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Layout saved successfully")
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar layout: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error saving layout", e)
        }
    }

    private fun resetLayout() {
        try {
            editorView?.resetToDefault()
            Toast.makeText(this, "Layout restaurado ao padrão!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Layout reset to default")
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao resetar layout: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error resetting layout", e)
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "onKeyDown: $keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Log.d(TAG, "Back button pressed")
                if (drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
                    Log.d(TAG, "Closing drawer")
                    drawerLayout?.closeDrawer(GravityCompat.START)
                } else {
                    Log.d(TAG, "Opening drawer")
                    drawerLayout?.openDrawer(GravityCompat.START)
                }
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                Log.d(TAG, "Menu button pressed")
                if (drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
                    drawerLayout?.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout?.openDrawer(GravityCompat.START)
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed called")
        if (drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            Log.d(TAG, "Closing drawer from onBackPressed")
            drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            Log.d(TAG, "Opening drawer from onBackPressed")
            drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Manter tela cheia quando a activity ganhar foco
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
} 