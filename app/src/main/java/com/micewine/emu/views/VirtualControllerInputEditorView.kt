package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolution
import com.micewine.emu.utils.XInputLayoutPreferences
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_RECTANGLE
import kotlin.math.roundToInt
import kotlin.math.sqrt

class VirtualControllerInputEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        strokeWidth = 16F
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    
    private val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 120F
        typeface = context.resources.getFont(R.font.quicksand)
    }

    private val selectionPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 8F
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2F
        isAntiAlias = true
        alpha = 100
    }

    // Paths para D-pad (igual ao VirtualControllerInputView)
    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()
    private val startButton: Path = Path()
    private val selectButton: Path = Path()

    // Controles editáveis
    private val editableButtons = mutableListOf<EditableButton>()
    private val editableAnalogs = mutableListOf<EditableAnalog>()
    private val editableDPads = mutableListOf<EditableDPad>()
    
    // Controle selecionado para edição
    private var selectedControl: EditableControl? = null
    private var selectedControlType: ControlType? = null
    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // Utilitário para preferências
    private val preferences = XInputLayoutPreferences(context)

    // Interface para controles editáveis
    interface EditableControl {
        var x: Float
        var y: Float
        var alpha: Int
        var radius: Float
        var isSelected: Boolean
    }

    enum class ControlType {
        BUTTON, ANALOG, DPAD
    }

    data class EditableButton(
        val id: Int,
        override var x: Float,
        override var y: Float,
        override var radius: Float,
        val shape: Int,
        val name: String,
        override var alpha: Int = 200,
        override var isSelected: Boolean = false
    ) : EditableControl

    data class EditableAnalog(
        val id: Int,
        override var x: Float,
        override var y: Float,
        override var radius: Float,
        val name: String,
        override var alpha: Int = 200,
        override var isSelected: Boolean = false
    ) : EditableControl

    data class EditableDPad(
        val id: Int,
        override var x: Float,
        override var y: Float,
        override var radius: Float,
        val name: String,
        override var alpha: Int = 200,
        override var isSelected: Boolean = false
    ) : EditableControl

    init {
        loadExistingControls()
    }

    private fun loadExistingControls() {
        // Tentar carregar configurações salvas primeiro
        val savedButtons = preferences.loadButtons()
        val savedAnalogs = preferences.loadAnalogs()
        val savedDPads = preferences.loadDPads()

        if (savedButtons.isNotEmpty() && savedAnalogs.isNotEmpty() && savedDPads.isNotEmpty()) {
            // Carregar configurações salvas
            loadSavedControls(savedButtons, savedAnalogs, savedDPads)
        } else {
            // Carregar configurações padrão
            loadDefaultControls()
        }
    }

    private fun loadSavedControls(
        savedButtons: List<EditableButton>,
        savedAnalogs: List<EditableAnalog>,
        savedDPads: List<EditableDPad>
    ) {
        // Carregar botões salvos
        editableButtons.clear()
        editableButtons.addAll(savedButtons)

        // Carregar analógicos salvos
        editableAnalogs.clear()
        editableAnalogs.addAll(savedAnalogs)

        // Carregar D-pads salvos
        editableDPads.clear()
        editableDPads.addAll(savedDPads)

        adjustForScreenResolution()
        centerControlsOnScreen()
    }

    private fun loadDefaultControls() {
        // Carregar botões padrão do VirtualControllerInputView
        editableButtons.clear()
        editableButtons.addAll(listOf(
            EditableButton(VirtualControllerInputView.A_BUTTON, 2065F, 910F, 180F, SHAPE_CIRCLE, "A"),
            EditableButton(VirtualControllerInputView.B_BUTTON, 2205F, 735F, 180F, SHAPE_CIRCLE, "B"),
            EditableButton(VirtualControllerInputView.X_BUTTON, 1925F, 735F, 180F, SHAPE_CIRCLE, "X"),
            EditableButton(VirtualControllerInputView.Y_BUTTON, 2065F, 560F, 180F, SHAPE_CIRCLE, "Y"),
            EditableButton(VirtualControllerInputView.START_BUTTON, 1330F, 980F, 130F, SHAPE_CIRCLE, "START"),
            EditableButton(VirtualControllerInputView.SELECT_BUTTON, 1120F, 980F, 130F, SHAPE_CIRCLE, "SELECT"),
            EditableButton(VirtualControllerInputView.LB_BUTTON, 280F, 300F, 260F, SHAPE_RECTANGLE, "LB"),
            EditableButton(VirtualControllerInputView.LT_BUTTON, 280F, 140F, 260F, SHAPE_RECTANGLE, "LT"),
            EditableButton(VirtualControllerInputView.RB_BUTTON, 2065F, 300F, 260F, SHAPE_RECTANGLE, "RB"),
            EditableButton(VirtualControllerInputView.RT_BUTTON, 2065F, 140F, 260F, SHAPE_RECTANGLE, "RT"),
            EditableButton(VirtualControllerInputView.LS_BUTTON, 880F, 980F, 180F, SHAPE_CIRCLE, "LS"),
            EditableButton(VirtualControllerInputView.RS_BUTTON, 1560F, 980F, 180F, SHAPE_CIRCLE, "RS")
        ))

        // Carregar analógicos padrão do VirtualControllerInputView
        editableAnalogs.clear()
        editableAnalogs.addAll(listOf(
            EditableAnalog(VirtualControllerInputView.LEFT_ANALOG, 280F, 840F, 275F, "L"),
            EditableAnalog(0, 1750F, 480F, 275F, "R") // Touchpad
        ))

        // Carregar D-pad padrão do VirtualControllerInputView
        editableDPads.clear()
        editableDPads.addAll(listOf(
            EditableDPad(0, 640F, 480F, 250F, "D-PAD")
        ))

        adjustForScreenResolution()
        centerControlsOnScreen()
    }

    private fun adjustForScreenResolution() {
        val nativeResolution = getNativeResolution(context)
        val baseResolution = "2400x1080" // Resolução base do dispositivo

        if (baseResolution != nativeResolution) {
            val nativeSplit = nativeResolution.split("x").map { it.toFloat() }
            val processedSplit = baseResolution.split("x").map { it.toFloat() }

            val multiplierX = nativeSplit[0] / processedSplit[0] * 100F
            val multiplierY = nativeSplit[1] / processedSplit[1] * 100F

            editableButtons.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            
            editableAnalogs.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            
            editableDPads.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
        }
    }

    private fun centerControlsOnScreen() {
        // Junta todos os controles em uma lista
        val allControls = mutableListOf<EditableControl>()
        allControls.addAll(editableButtons)
        allControls.addAll(editableAnalogs)
        allControls.addAll(editableDPads)
        if (allControls.isEmpty()) return

        // Calcula o bounding box
        val minX = allControls.minOf { it.x - it.radius / 2 }
        val maxX = allControls.maxOf { it.x + it.radius / 2 }
        val minY = allControls.minOf { it.y - it.radius / 2 }
        val maxY = allControls.maxOf { it.y + it.radius / 2 }

        val controlsCenterX = (minX + maxX) / 2f
        val controlsCenterY = (minY + maxY) / 2f

        // Centro da tela
        val viewCenterX = width / 2f
        val viewCenterY = height / 2f

        // Deslocamento necessário
        val dx = viewCenterX - controlsCenterX
        val dy = viewCenterY - controlsCenterY

        // Aplica deslocamento
        editableButtons.forEach { it.x += dx; it.y += dy }
        editableAnalogs.forEach { it.x += dx; it.y += dy }
        editableDPads.forEach { it.x += dx; it.y += dy }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerControlsOnScreen()
    }

    private fun getButtonName(id: Int): String {
        return when (id) {
            VirtualControllerInputView.A_BUTTON -> "A"
            VirtualControllerInputView.B_BUTTON -> "B"
            VirtualControllerInputView.X_BUTTON -> "X"
            VirtualControllerInputView.Y_BUTTON -> "Y"
            VirtualControllerInputView.RB_BUTTON -> "RB"
            VirtualControllerInputView.LB_BUTTON -> "LB"
            VirtualControllerInputView.RT_BUTTON -> "RT"
            VirtualControllerInputView.LT_BUTTON -> "LT"
            VirtualControllerInputView.RS_BUTTON -> "RS"
            VirtualControllerInputView.LS_BUTTON -> "LS"
            else -> ""
        }
    }

    private fun drawDPad(path: Path, canvas: Canvas, dpad: EditableDPad) {
        paint.style = Paint.Style.STROKE
        paint.alpha = dpad.alpha
        paint.color = if (dpad.isSelected) Color.YELLOW else Color.WHITE
        paint.strokeWidth = 16F

        canvas.drawPath(path, paint)
    }

    // Função para ajustar o tamanho do texto dinamicamente
    private fun adjustTextSize(text: String, maxWidth: Float, paint: Paint, radius: Float) {
        paint.textSize = radius / 6F
        while (paint.measureText(text) > maxWidth - (maxWidth / 5F)) {
            paint.textSize--
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Desenhar grid
        for (i in 0..width) {
            if (i % GRID_SIZE == 0) {
                canvas.drawLine(i.toFloat(), 0F, i.toFloat(), height.toFloat(), gridPaint)
            }
        }

        for (i in 0..height) {
            if (i % GRID_SIZE == 0) {
                canvas.drawLine(0F, i.toFloat(), width.toFloat(), i.toFloat(), gridPaint)
            }
        }

        // Desenhar botões (igual ao VirtualControllerInputView)
        editableButtons.forEach { button ->
            if (button.isSelected) {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.color = Color.YELLOW
                textPaint.color = Color.BLACK
            } else {
                paint.style = Paint.Style.STROKE
                paint.color = Color.WHITE
                textPaint.color = Color.WHITE
            }
            paint.alpha = button.alpha
            paint.strokeWidth = 16F
            textPaint.alpha = button.alpha

            when (button.shape) {
                SHAPE_CIRCLE -> {
                    canvas.drawCircle(button.x, button.y, button.radius / 2, paint)
                }
                SHAPE_RECTANGLE -> {
                    canvas.drawRoundRect(
                        button.x - button.radius / 2,
                        button.y - button.radius / 4,
                        button.x + button.radius / 2,
                        button.y + button.radius / 4,
                        32F,
                        32F,
                        paint
                    )
                }
            }

            when (button.id) {
                VirtualControllerInputView.START_BUTTON -> {
                    paint.strokeWidth = button.radius / 15F
                    startButton.apply {
                        reset()
                        moveTo(button.x - button.radius / 3, button.y - button.radius / 8)
                        lineTo(button.x - button.radius / 3 + button.radius / 3, button.y - button.radius / 8)
                        moveTo(button.x - button.radius / 3, button.y)
                        lineTo(button.x - button.radius / 3 + button.radius / 3, button.y)
                        moveTo(button.x - button.radius / 3, button.y + button.radius / 8)
                        lineTo(button.x - button.radius / 3 + button.radius / 3, button.y + button.radius / 8)
                        close()
                    }
                    paint.color = if (button.isSelected) Color.BLACK else Color.WHITE
                    paint.alpha = button.alpha
                    canvas.drawPath(startButton, paint)
                }
                VirtualControllerInputView.SELECT_BUTTON -> {
                    paint.strokeWidth = button.radius / 15F
                    selectButton.apply {
                        reset()
                        moveTo(button.x - button.radius / 4 + button.radius / 20F, button.y - button.radius / 4 + button.radius / 3F)
                        lineTo(button.x - button.radius / 4 + button.radius / 20F, button.y - button.radius / 4)
                        lineTo(button.x - button.radius / 4 + button.radius / 20F + button.radius / 2F, button.y - button.radius / 4)
                        lineTo(button.x - button.radius / 4 + button.radius / 20F + button.radius / 2F, button.y - button.radius / 4 + button.radius / 6F)
                        lineTo(button.x - button.radius / 4 + button.radius / 20F + button.radius / 2F, button.y - button.radius / 4)
                        lineTo(button.x - button.radius / 4 + button.radius / 20F, button.y - button.radius / 4)
                        close()
                        moveTo(button.x - button.radius / 4 + button.radius / 3F, button.y - button.radius / 4 + button.radius / 2.5F)
                        lineTo(button.x - button.radius / 4 + button.radius / 1.5F, button.y - button.radius / 4 + button.radius / 2.5F)
                        lineTo(button.x - button.radius / 4 + button.radius / 1.5F, button.y - button.radius / 4 + button.radius / 1.2F)
                        lineTo(button.x - button.radius / 4 + button.radius / 3F, button.y - button.radius / 4 + button.radius / 1.2F)
                        lineTo(button.x - button.radius / 4 + button.radius / 3F, button.y - button.radius / 4 + button.radius / 2.8F)
                        lineTo(button.x - button.radius / 4 + button.radius / 3F, button.y - button.radius / 4 + button.radius / 1.2F)
                        lineTo(button.x - button.radius / 4 + button.radius / 1.5F, button.y - button.radius / 4 + button.radius / 1.2F)
                        lineTo(button.x - button.radius / 4 + button.radius / 1.5F, button.y - button.radius / 4 + button.radius / 2.5F)
                        close()
                    }
                    paint.color = if (button.isSelected) Color.BLACK else Color.WHITE
                    paint.alpha = button.alpha
                    canvas.drawPath(selectButton, paint)
                }
                else -> {
                    val buttonName = getButtonName(button.id)
                    adjustTextSize(buttonName, button.radius, textPaint, button.radius)
                    val offset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
                    canvas.drawText(buttonName, button.x, button.y - offset - 4, textPaint)
                }
            }
        }

        // Desenhar analógicos (igual ao VirtualControllerInputView)
        editableAnalogs.forEach { analog ->
            if (analog.isSelected) {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.color = Color.YELLOW
                textPaint.color = Color.BLACK
            } else {
                paint.style = Paint.Style.STROKE
                paint.color = Color.WHITE
                textPaint.color = Color.WHITE
            }
            paint.alpha = analog.alpha

            // Desenhar círculo externo
            canvas.drawCircle(analog.x, analog.y, analog.radius / 2, paint)

            // Desenhar círculo interno
            paint.style = Paint.Style.FILL
            paint.color = if (analog.isSelected) Color.BLACK else Color.WHITE
            canvas.drawCircle(analog.x, analog.y, analog.radius / 4, paint)

            // Desenhar nome do analógico
            textPaint.alpha = analog.alpha
            
            adjustTextSize(analog.name, analog.radius, textPaint, analog.radius)
            val offset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
            canvas.drawText(analog.name, analog.x, analog.y - offset - 4, textPaint)
        }

        // Desenhar D-pad (igual ao VirtualControllerInputView)
        editableDPads.forEach { dpad ->
            canvas.apply {
                dpadLeft.apply {
                    reset()
                    moveTo(dpad.x - 20, dpad.y)
                    lineTo(dpad.x - 20 - dpad.radius / 4, dpad.y - dpad.radius / 4)
                    lineTo(dpad.x - 20 - dpad.radius / 4 - dpad.radius / 2, dpad.y - dpad.radius / 4)
                    lineTo(
                        dpad.x - 20 - dpad.radius / 4 - dpad.radius / 2,
                        dpad.y - dpad.radius / 4 + dpad.radius / 2
                    )
                    lineTo(dpad.x - 20 - dpad.radius / 4, dpad.y - dpad.radius / 4 + dpad.radius / 2)
                    lineTo(dpad.x - 20, dpad.y)
                    close()
                }
                dpadUp.apply {
                    reset()
                    moveTo(dpad.x, dpad.y - 20)
                    lineTo(dpad.x - dpad.radius / 4, dpad.y - 20 - dpad.radius / 4)
                    lineTo(dpad.x - dpad.radius / 4, dpad.y - 20 - dpad.radius / 4 - dpad.radius / 2)
                    lineTo(
                        dpad.x - dpad.radius / 4 + dpad.radius / 2,
                        dpad.y - 20 - dpad.radius / 4 - dpad.radius / 2
                    )
                    lineTo(dpad.x - dpad.radius / 4 + dpad.radius / 2, dpad.y - 20 - dpad.radius / 4)
                    lineTo(dpad.x, dpad.y - 20)
                    close()
                }
                dpadRight.apply {
                    reset()
                    moveTo(dpad.x + 20, dpad.y)
                    lineTo(dpad.x + 20 + dpad.radius / 4, dpad.y - dpad.radius / 4)
                    lineTo(dpad.x + 20 + dpad.radius / 4 + dpad.radius / 2, dpad.y - dpad.radius / 4)
                    lineTo(
                        dpad.x + 20 + dpad.radius / 4 + dpad.radius / 2,
                        dpad.y - dpad.radius / 4 + dpad.radius / 2
                    )
                    lineTo(dpad.x + 20 + dpad.radius / 4, dpad.y - dpad.radius / 4 + dpad.radius / 2)
                    lineTo(dpad.x + 20, dpad.y)
                    close()
                }
                dpadDown.apply {
                    reset()
                    moveTo(dpad.x, dpad.y + 20)
                    lineTo(dpad.x - dpad.radius / 4, dpad.y + 20 + dpad.radius / 4)
                    lineTo(dpad.x - dpad.radius / 4, dpad.y + 20 + dpad.radius / 4 + dpad.radius / 2)
                    lineTo(
                        dpad.x - dpad.radius / 4 + dpad.radius / 2,
                        dpad.y + 20 + dpad.radius / 4 + dpad.radius / 2
                    )
                    lineTo(dpad.x - dpad.radius / 4 + dpad.radius / 2, dpad.y + 20 + dpad.radius / 4)
                    lineTo(dpad.x, dpad.y + 20)
                    close()
                }

                // Desenhar todas as setas do D-pad
                drawDPad(dpadUp, canvas, dpad)
                drawDPad(dpadDown, canvas, dpad)
                drawDPad(dpadLeft, canvas, dpad)
                drawDPad(dpadRight, canvas, dpad)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y
                
                // Desselecionar TODOS os controles primeiro
                editableButtons.forEach { it.isSelected = false }
                editableAnalogs.forEach { it.isSelected = false }
                editableDPads.forEach { it.isSelected = false }
                
                // Verificar se tocou em algum controle
                selectedControl = null
                selectedControlType = null
                
                // Verificar D-pads
                editableDPads.forEach { dpad ->
                    if (isPointInCircle(touchX, touchY, dpad.x, dpad.y, dpad.radius / 2)) {
                        selectedControl = dpad
                        selectedControlType = ControlType.DPAD
                        dpad.isSelected = true
                        isDragging = true
                        lastTouchX = touchX
                        lastTouchY = touchY
                        invalidate()
                        return true
                    }
                }
                
                // Verificar analógicos
                editableAnalogs.forEach { analog ->
                    if (isPointInCircle(touchX, touchY, analog.x, analog.y, analog.radius / 2)) {
                        selectedControl = analog
                        selectedControlType = ControlType.ANALOG
                        analog.isSelected = true
                        isDragging = true
                        lastTouchX = touchX
                        lastTouchY = touchY
                        invalidate()
                        return true
                    }
                }
                
                // Verificar botões
                editableButtons.forEach { button ->
                    if (isPointInControl(touchX, touchY, button)) {
                        selectedControl = button
                        selectedControlType = ControlType.BUTTON
                        button.isSelected = true
                        isDragging = true
                        lastTouchX = touchX
                        lastTouchY = touchY
                        invalidate()
                        return true
                    }
                }
                
                invalidate()
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && selectedControl != null) {
                    val touchX = event.x
                    val touchY = event.y
                    
                    // Snap to grid
                    val snappedX = (touchX / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                    val snappedY = (touchY / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                    
                    selectedControl!!.x = snappedX
                    selectedControl!!.y = snappedY
                    
                    lastTouchX = touchX
                    lastTouchY = touchY
                    
                    invalidate()
                }
            }
            
            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        
        return true
    }

    private fun isPointInCircle(px: Float, py: Float, cx: Float, cy: Float, radius: Float): Boolean {
        val distance = sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy))
        return distance <= radius
    }

    private fun isPointInControl(px: Float, py: Float, button: EditableButton): Boolean {
        return when (button.shape) {
            SHAPE_CIRCLE -> isPointInCircle(px, py, button.x, button.y, button.radius / 2)
            SHAPE_RECTANGLE -> {
                px >= button.x - button.radius / 2 &&
                px <= button.x + button.radius / 2 &&
                py >= button.y - button.radius / 4 &&
                py <= button.y + button.radius / 4
            }
            else -> false
        }
    }

    // Métodos para acessar os controles editados
    fun getEditedButtons(): List<EditableButton> = editableButtons.toList()
    fun getEditedAnalogs(): List<EditableAnalog> = editableAnalogs.toList()
    fun getEditedDPads(): List<EditableDPad> = editableDPads.toList()

    // Retornar o controle selecionado
    fun getSelectedControl(): EditableControl? {
        return selectedControl
    }

    fun setButtonAlpha(buttonId: Int, alpha: Int) {
        editableButtons.find { it.id == buttonId }?.alpha = alpha
        invalidate()
    }

    fun setAnalogAlpha(analogId: Int, alpha: Int) {
        editableAnalogs.find { it.id == analogId }?.alpha = alpha
        invalidate()
    }

    fun setDPadAlpha(dpadId: Int, alpha: Int) {
        editableDPads.find { it.id == dpadId }?.alpha = alpha
        invalidate()
    }

    fun saveCurrentLayout() {
        preferences.saveButtons(editableButtons)
        preferences.saveAnalogs(editableAnalogs)
        preferences.saveDPads(editableDPads)
    }

    fun resetToDefault() {
        preferences.clearAll()
        loadDefaultControls()
        invalidate()
    }



    companion object {
        const val GRID_SIZE = 30
    }
} 