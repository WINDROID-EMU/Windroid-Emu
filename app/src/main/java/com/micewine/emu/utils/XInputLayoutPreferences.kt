package com.micewine.emu.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.views.VirtualControllerInputEditorView

class XInputLayoutPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "xinput_layout_prefs", Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_BUTTONS = "xinput_buttons"
        private const val KEY_ANALOGS = "xinput_analogs"
        private const val KEY_DPADS = "xinput_dpads"
    }

    // Salvar configurações dos botões
    fun saveButtons(buttons: List<VirtualControllerInputEditorView.EditableButton>) {
        val buttonsJson = gson.toJson(buttons)
        sharedPreferences.edit().putString(KEY_BUTTONS, buttonsJson).apply()
    }

    // Carregar configurações dos botões
    fun loadButtons(): List<VirtualControllerInputEditorView.EditableButton> {
        val buttonsJson = sharedPreferences.getString(KEY_BUTTONS, null)
        return if (buttonsJson != null) {
            try {
                val type = object : TypeToken<List<VirtualControllerInputEditorView.EditableButton>>() {}.type
                gson.fromJson(buttonsJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Salvar configurações dos analógicos
    fun saveAnalogs(analogs: List<VirtualControllerInputEditorView.EditableAnalog>) {
        val analogsJson = gson.toJson(analogs)
        sharedPreferences.edit().putString(KEY_ANALOGS, analogsJson).apply()
    }

    // Carregar configurações dos analógicos
    fun loadAnalogs(): List<VirtualControllerInputEditorView.EditableAnalog> {
        val analogsJson = sharedPreferences.getString(KEY_ANALOGS, null)
        return if (analogsJson != null) {
            try {
                val type = object : TypeToken<List<VirtualControllerInputEditorView.EditableAnalog>>() {}.type
                gson.fromJson(analogsJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Salvar configurações dos D-pads
    fun saveDPads(dpads: List<VirtualControllerInputEditorView.EditableDPad>) {
        val dpadsJson = gson.toJson(dpads)
        sharedPreferences.edit().putString(KEY_DPADS, dpadsJson).apply()
    }

    // Carregar configurações dos D-pads
    fun loadDPads(): List<VirtualControllerInputEditorView.EditableDPad> {
        val dpadsJson = sharedPreferences.getString(KEY_DPADS, null)
        return if (dpadsJson != null) {
            try {
                val type = object : TypeToken<List<VirtualControllerInputEditorView.EditableDPad>>() {}.type
                gson.fromJson(dpadsJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Limpar todas as configurações (reset)
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    // Verificar se existem configurações salvas
    fun hasSavedLayout(): Boolean {
        return sharedPreferences.contains(KEY_BUTTONS) ||
               sharedPreferences.contains(KEY_ANALOGS) ||
               sharedPreferences.contains(KEY_DPADS)
    }
} 