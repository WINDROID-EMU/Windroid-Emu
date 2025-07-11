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
import com.micewine.emu.LorieView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolution
import com.micewine.emu.controller.ControllerUtils.DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT
import com.micewine.emu.controller.ControllerUtils.LEFT_DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT_UP
import com.micewine.emu.controller.ControllerUtils.RIGHT
import com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN
import com.micewine.emu.controller.ControllerUtils.RIGHT_UP
import com.micewine.emu.controller.ControllerUtils.UP
import com.micewine.emu.controller.ControllerUtils.axisToByteArray
import com.micewine.emu.controller.ControllerUtils.connectedVirtualControllers
import com.micewine.emu.controller.ControllerUtils.normalizeAxisValue
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_DPAD
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_RECTANGLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_SQUARE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.detectClick
import kotlin.math.sqrt
import com.micewine.emu.utils.XInputLayoutPreferences

class VirtualControllerInputView @JvmOverloads constructor(
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

    private var lorieView: LorieView = LorieView(context)
    private var isFingerPressingButton = false
    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()
    private val startButton: Path = Path()
    private val selectButton: Path = Path()
    private val buttonList: MutableList<VirtualControllerButton> = mutableListOf()
    private var dpad: VirtualXInputDPad = VirtualXInputDPad(0, 640F, 480F, 250F)
    private var leftAnalog: VirtualXInputAnalog = VirtualXInputAnalog(LEFT_ANALOG, 280F, 840F, 275F, false, 0, 0F, 0F)
    private var rightTouchPad: VirtualXInputTouchPad = VirtualXInputTouchPad(0, 1750F, 480F, 275F, false, 0, 0F, 0F)

    // Utilitário para preferências
    private val preferences = XInputLayoutPreferences(context)

    private fun adjustButtons() {
        val nativeResolution = getNativeResolution(context)
        val baseResolution = "2400x1080" // My Device Resolution

        if (baseResolution != nativeResolution) {
            val nativeSplit = nativeResolution.split("x").map { it.toFloat() }
            val processedSplit = baseResolution.split("x").map { it.toFloat() }

            val multiplierX = nativeSplit[0] / processedSplit[0] * 100F
            val multiplierY = nativeSplit[1] / processedSplit[1] * 100F

            buttonList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            leftAnalog.let {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            dpad.let {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
        }
    }

    private fun addButton(id: Int, x: Float, y: Float, radius: Float, shape: Int) {
        buttonList.add(
            VirtualControllerButton(id, x, y, radius, shape)
        )
    }

    init {
        loadLayoutConfiguration()
    }

    private fun loadLayoutConfiguration() {
        // Tentar carregar configurações salvas primeiro
        val savedButtons = preferences.loadButtons()
        val savedAnalogs = preferences.loadAnalogs()
        val savedDPads = preferences.loadDPads()

        if (savedButtons.isNotEmpty() && savedAnalogs.isNotEmpty() && savedDPads.isNotEmpty()) {
            // Carregar configurações salvas
            loadSavedLayout(savedButtons, savedAnalogs, savedDPads)
        } else {
            // Carregar configurações padrão
            loadDefaultLayout()
        }
    }

    private fun loadSavedLayout(
        savedButtons: List<VirtualControllerInputEditorView.EditableButton>,
        savedAnalogs: List<VirtualControllerInputEditorView.EditableAnalog>,
        savedDPads: List<VirtualControllerInputEditorView.EditableDPad>
    ) {
        // Carregar botões salvos
        buttonList.clear()
        savedButtons.forEach { savedButton ->
            addButton(savedButton.id, savedButton.x, savedButton.y, savedButton.radius, savedButton.shape)
        }

        // Carregar analógicos salvos
        val leftAnalogData = savedAnalogs.find { it.id == VirtualControllerInputView.LEFT_ANALOG }
        val rightTouchPadData = savedAnalogs.find { it.id == 0 } // Touchpad

        leftAnalog = if (leftAnalogData != null) {
            VirtualXInputAnalog(
                leftAnalogData.id,
                leftAnalogData.x,
                leftAnalogData.y,
                leftAnalogData.radius,
                false,
                0,
                0F,
                0F
            )
        } else {
            VirtualXInputAnalog(LEFT_ANALOG, 280F, 840F, 275F, false, 0, 0F, 0F)
        }

        rightTouchPad = if (rightTouchPadData != null) {
            VirtualXInputTouchPad(
                rightTouchPadData.id,
                rightTouchPadData.x,
                rightTouchPadData.y,
                rightTouchPadData.radius,
                false,
                0,
                0F,
                0F
            )
        } else {
            VirtualXInputTouchPad(0, 1750F, 480F, 275F, false, 0, 0F, 0F)
        }

        // Carregar D-pad salvo
        val dpadData = savedDPads.firstOrNull()
        dpad = if (dpadData != null) {
            VirtualXInputDPad(dpadData.id, dpadData.x, dpadData.y, dpadData.radius)
        } else {
            VirtualXInputDPad(0, 640F, 480F, 250F)
        }

        adjustButtons()
    }

    private fun loadDefaultLayout() {
        addButton(A_BUTTON, 2065F, 910F, 180F, SHAPE_CIRCLE)
        addButton(B_BUTTON, 2205F, 735F, 180F, SHAPE_CIRCLE)
        addButton(X_BUTTON, 1925F, 735F, 180F, SHAPE_CIRCLE)
        addButton(Y_BUTTON, 2065F, 560F, 180F, SHAPE_CIRCLE)
        addButton(START_BUTTON, 1330F, 980F, 130F, SHAPE_CIRCLE)
        addButton(SELECT_BUTTON, 1120F, 980F, 130F, SHAPE_CIRCLE)
        addButton(LB_BUTTON, 280F, 300F, 260F, SHAPE_RECTANGLE)
        addButton(LT_BUTTON, 280F, 140F, 260F, SHAPE_RECTANGLE)
        addButton(RB_BUTTON, 2065F, 300F, 260F, SHAPE_RECTANGLE)
        addButton(RT_BUTTON, 2065F, 140F, 260F, SHAPE_RECTANGLE)
        addButton(LS_BUTTON, 880F, 980F, 180F, SHAPE_CIRCLE)
        addButton(RS_BUTTON, 1560F, 980F, 180F, SHAPE_CIRCLE)

        leftAnalog = VirtualXInputAnalog(LEFT_ANALOG, 280F, 840F, 275F, false, 0, 0F, 0F)
        dpad = VirtualXInputDPad(0, 640F, 480F, 250F)
        rightTouchPad = VirtualXInputTouchPad(0, 1750F, 480F, 275F)

        adjustButtons()
    }

    private fun getButtonName(id: Int): String {
        return when (id) {
            A_BUTTON -> "A"
            B_BUTTON -> "B"
            X_BUTTON -> "X"
            Y_BUTTON -> "Y"
            RB_BUTTON -> "RB"
            LB_BUTTON -> "LB"
            RT_BUTTON -> "RT"
            LT_BUTTON -> "LT"
            RS_BUTTON -> "RS"
            LS_BUTTON -> "LS"
            else -> ""
        }
    }

    // Função para ajustar o tamanho do texto dinamicamente
    private fun adjustTextSize(text: String, maxWidth: Float, paint: Paint, radius: Float) {
        paint.textSize = radius / 6F
        while (paint.measureText(text) > maxWidth - (maxWidth / 5F)) {
            paint.textSize--
        }
    }

    private fun drawDPad(path: Path, pressed: Boolean, canvas: Canvas) {
        if (pressed) {
            paint.style = Paint.Style.FILL_AND_STROKE
        } else {
            paint.style = Paint.Style.STROKE
        }
        paint.alpha = 200

        canvas.drawPath(path, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonList.forEach {
            if (it.isPressed) {
                paint.style = Paint.Style.FILL_AND_STROKE
                textPaint.color = Color.BLACK
            } else {
                paint.style = Paint.Style.STROKE
                textPaint.color = Color.WHITE
            }
            paint.color = Color.WHITE
            paint.alpha = 200
            paint.strokeWidth = 16F
            textPaint.alpha = 200

            when (it.shape) {
                SHAPE_CIRCLE -> {
                    canvas.drawCircle(it.x, it.y, it.radius / 2, paint)
                }
                SHAPE_RECTANGLE -> {
                    canvas.drawRoundRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 4,
                        it.x + it.radius / 2,
                        it.y + it.radius / 4,
                        32F,
                        32F,
                        paint
                    )
                }
            }

            when (it.id) {
                START_BUTTON -> {
                    paint.strokeWidth = it.radius / 15F
                    startButton.apply {
                        reset()
                        // Centralizar as três linhas horizontais
                        val lineLength = it.radius * 0.5f
                        val lineSpacing = it.radius * 0.18f
                        val centerX = it.x
                        val centerY = it.y
                        // Linha superior
                        moveTo(centerX - lineLength / 2, centerY - lineSpacing)
                        lineTo(centerX + lineLength / 2, centerY - lineSpacing)
                        // Linha do meio
                        moveTo(centerX - lineLength / 2, centerY)
                        lineTo(centerX + lineLength / 2, centerY)
                        // Linha inferior
                        moveTo(centerX - lineLength / 2, centerY + lineSpacing)
                        lineTo(centerX + lineLength / 2, centerY + lineSpacing)
                        close()
                    }
                    paint.color = if (it.isPressed) Color.BLACK else Color.WHITE
                    paint.alpha = 200

                    canvas.drawPath(startButton, paint)
                }
                SELECT_BUTTON -> {
                    paint.strokeWidth = it.radius / 15F
                    selectButton.apply {
                        reset()
                        // Centralizar um retângulo (símbolo de menu)
                        val rectWidth = it.radius * 0.5f
                        val rectHeight = it.radius * 0.22f
                        val centerX = it.x
                        val centerY = it.y
                        // Retângulo principal
                        moveTo(centerX - rectWidth / 2, centerY - rectHeight / 2)
                        lineTo(centerX + rectWidth / 2, centerY - rectHeight / 2)
                        lineTo(centerX + rectWidth / 2, centerY + rectHeight / 2)
                        lineTo(centerX - rectWidth / 2, centerY + rectHeight / 2)
                        close()
                        // Linha interna (menu)
                        val innerLineY = centerY
                        val innerLineLength = rectWidth * 0.7f
                        moveTo(centerX - innerLineLength / 2, innerLineY)
                        lineTo(centerX + innerLineLength / 2, innerLineY)
                    }
                    paint.color = if (it.isPressed) Color.BLACK else Color.WHITE
                    paint.alpha = 200

                    canvas.drawPath(selectButton, paint)
                }
                else -> {
                    val buttonName = getButtonName(it.id)
                    adjustTextSize(buttonName, it.radius, textPaint, it.radius)
                    val offset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
                    // Deixar as letras dos botões A, B, X, Y em negrito
                    if (it.id == A_BUTTON || it.id == B_BUTTON || it.id == X_BUTTON || it.id == Y_BUTTON) {
                        textPaint.typeface = android.graphics.Typeface.create(textPaint.typeface, android.graphics.Typeface.BOLD)
                    } else {
                        textPaint.typeface = android.graphics.Typeface.create(textPaint.typeface, android.graphics.Typeface.NORMAL)
                    }
                    canvas.drawText(buttonName, it.x, it.y - offset - 4, textPaint)
                }
            }
        }
        leftAnalog.let {
            var analogX = it.x + it.fingerX
            var analogY = it.y + it.fingerY

            val distSquared = (it.fingerX * it.fingerX) + (it.fingerY * it.fingerY)
            val maxDist = (it.radius / 4) * (it.radius / 4)

            if (distSquared > maxDist) {
                val scale = (it.radius / 4) / sqrt(distSquared)
                analogX = it.x + (it.fingerX * scale)
                analogY = it.y + (it.fingerY * scale)
            }

            paint.color = Color.WHITE
            paint.alpha = 200

            paint.style = Paint.Style.STROKE
            canvas.drawCircle(it.x, it.y, it.radius / 2, paint)

            paint.style = Paint.Style.FILL
            canvas.drawCircle(analogX, analogY, it.radius / 4, paint)
        }
        rightTouchPad.let {
            paint.style = Paint.Style.STROKE
            canvas.drawCircle(it.x, it.y, it.radius / 2, paint)

            var analogX = it.x + it.fingerX
            var analogY = it.y + it.fingerY

            val distSquared = (it.fingerX * it.fingerX) + (it.fingerY * it.fingerY)
            val maxDist = (it.radius / 4) * (it.radius / 4)

            if (distSquared > maxDist) {
                val scale = (it.radius / 4) / sqrt(distSquared)
                analogX = it.x + (it.fingerX * scale)
                analogY = it.y + (it.fingerY * scale)
            }

            paint.style = Paint.Style.FILL
            canvas.drawCircle(analogX, analogY, it.radius / 4, paint)
        }
        dpad.let {
            canvas.apply {
                dpadLeft.apply {
                    reset()
                    moveTo(it.x - 20, it.y)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4)
                    lineTo(
                        it.x - 20 - it.radius / 4 - it.radius / 2,
                        it.y - it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20, it.y)
                    close()
                }
                dpadUp.apply {
                    reset()
                    moveTo(it.x, it.y - 20)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(
                        it.x - it.radius / 4 + it.radius / 2,
                        it.y - 20 - it.radius / 4 - it.radius / 2
                    )
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4)
                    lineTo(it.x, it.y - 20)
                    close()
                }
                dpadRight.apply {
                    reset()
                    moveTo(it.x + 20, it.y)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4)
                    lineTo(
                        it.x + 20 + it.radius / 4 + it.radius / 2,
                        it.y - it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20, it.y)
                    close()
                }
                dpadDown.apply {
                    reset()
                    moveTo(it.x, it.y + 20)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(
                        it.x - it.radius / 4 + it.radius / 2,
                        it.y + 20 + it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4)
                    lineTo(it.x, it.y + 20)
                    close()
                }

                drawDPad(dpadUp, it.dpadStatus in listOf(UP, RIGHT_UP, LEFT_UP), canvas)
                drawDPad(dpadDown, it.dpadStatus in listOf(DOWN, RIGHT_DOWN, LEFT_DOWN), canvas)
                drawDPad(dpadLeft, it.dpadStatus in listOf(LEFT, LEFT_DOWN, LEFT_UP), canvas)
                drawDPad(dpadRight, it.dpadStatus in listOf(RIGHT, RIGHT_DOWN, RIGHT_UP), canvas)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (virtualXInputControllerId == -1) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, it.shape)) {
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        handleButton(it, true)

                        return@forEach
                    }
                }
                leftAnalog.let {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        it.fingerX = posX
                        it.fingerY = posY

                        val maxDist = it.radius / 4
                        var normX = posX / maxDist
                        var normY = posY / maxDist
                        val magnitude = sqrt(normX * normX + normY * normY)
                        if (magnitude > 1f) {
                            normX /= magnitude
                            normY /= magnitude
                        }
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, normalizeAxisValue(normX))
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, normalizeAxisValue(-normY))

                        return@let
                    }
                }
                rightTouchPad.let {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)
                    }
                }
                dpad.let {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_DPAD)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        it.fingerX = posX
                        it.fingerY = posY

                        when {
                            (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = RIGHT
                            (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = LEFT
                            (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DOWN
                            (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = UP
                            (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = RIGHT_DOWN
                            (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = RIGHT_UP
                            (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = LEFT_DOWN
                            (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = LEFT_UP

                            else -> it.dpadStatus = 0
                        }

                        connectedVirtualControllers[virtualXInputControllerId].dpadStatus = it.dpadStatus

                        return@let
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    isFingerPressingButton = false

                    buttonList.forEach {
                        if (it.fingerId == event.getPointerId(i)) {
                            it.isPressed = true

                            isFingerPressingButton = true

                            handleButton(it, true)
                        }
                    }
                    leftAnalog.let {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            isFingerPressingButton = true

                            val maxDist = it.radius / 4
                            var normX = posX / maxDist
                            var normY = posY / maxDist
                            val magnitude = sqrt(normX * normX + normY * normY)
                            if (magnitude > 1f) {
                                normX /= magnitude
                                normY /= magnitude
                            }
                            axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, normalizeAxisValue(normX))
                            axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, normalizeAxisValue(-normY))
                        }
                    }
                    rightTouchPad.let {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            val maxDist = it.radius / 4
                            var normX = posX / maxDist
                            var normY = posY / maxDist
                            val magnitude = sqrt(normX * normX + normY * normY)
                            if (magnitude > 1f) {
                                normX /= magnitude
                                normY /= magnitude
                            }
                            axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].rx, normalizeAxisValue(normX))
                            axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ry, normalizeAxisValue(-normY))
                        }
                    }
                    dpad.let {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            isFingerPressingButton = true

                            when {
                                (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = RIGHT
                                (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = LEFT
                                (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DOWN
                                (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = UP
                                (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = RIGHT_DOWN
                                (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = RIGHT_UP
                                (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = LEFT_DOWN
                                (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = LEFT_UP

                                else -> it.dpadStatus = 0
                            }

                            connectedVirtualControllers[virtualXInputControllerId].dpadStatus = it.dpadStatus
                        }
                    }

                    if (!isFingerPressingButton && event.historySize > 0) {
                        val deltaX = event.getX(i) - event.getHistoricalX(i, 0)
                        val deltaY = event.getY(i) - event.getHistoricalY(i, 0)

                        if ((deltaX > 0.08 || deltaX < -0.08) && (deltaY > 0.08 || deltaY < -0.08)) {
                            lorieView.sendMouseEvent(deltaX, deltaY, BUTTON_UNDEFINED, false, true)
                        }
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        handleButton(it, false)
                    }
                }
                leftAnalog.let {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false

                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, 127)
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, 127)
                    }
                }
                rightTouchPad.let {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F
                        it.isPressed = false

                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].rx, 127)
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ry, 127)
                    }
                }
                dpad.let {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false
                        it.dpadStatus = 0

                        connectedVirtualControllers[virtualXInputControllerId].dpadStatus = 0
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    it.fingerId = -1

                    handleButton(it, false)
                }
                leftAnalog.let {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false

                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, 127)
                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, 127)
                }
                rightTouchPad.let {
                    it.fingerId = -1
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false

                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].rx, 127)
                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ry, 127)
                }
                dpad.let {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false
                    it.dpadStatus = 0

                    connectedVirtualControllers[virtualXInputControllerId].dpadStatus = 0
                }

                invalidate()
            }
        }

        return true
    }

    private fun handleButton(button: VirtualControllerButton, pressed: Boolean) {
        button.isPressed = pressed

        when (button.id) {
            A_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].aPressed = pressed
            }
            B_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].bPressed = pressed
            }
            X_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].xPressed = pressed
            }
            Y_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].yPressed = pressed
            }
            START_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].startPressed = pressed
            }
            SELECT_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].selectPressed = pressed
            }
            LB_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].lbPressed = pressed
            }
            LT_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].lt[0] = if (pressed) 2 else 0
                connectedVirtualControllers[virtualXInputControllerId].lt[1] = if (pressed) 5 else 0
                connectedVirtualControllers[virtualXInputControllerId].lt[2] = if (pressed) 5 else 0
            }
            RB_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].rbPressed = pressed
            }
            RT_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].rt[0] = if (pressed) 2 else 0
                connectedVirtualControllers[virtualXInputControllerId].rt[1] = if (pressed) 5 else 0
                connectedVirtualControllers[virtualXInputControllerId].rt[2] = if (pressed) 5 else 0
            }
            LS_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].lsPressed = pressed
            }
            RS_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].rsPressed = pressed
            }
        }
    }

    class VirtualControllerButton(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var shape: Int,
        var fingerId: Int = -1,
        var isPressed: Boolean = false
    )

    class VirtualXInputDPad(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var fingerId: Int = -1,
        var isPressed: Boolean = false,
        var fingerX: Float = 0F,
        var fingerY: Float = 0F,
        var dpadStatus: Int = 0
    )

    class VirtualXInputAnalog(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var isPressed: Boolean = false,
        var fingerId: Int = 0,
        var fingerX: Float = 0F,
        var fingerY: Float = 0F
    )

    class VirtualXInputTouchPad(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var isPressed: Boolean = false,
        var fingerId: Int = 0,
        var fingerX: Float = 0F,
        var fingerY: Float = 0F
    )

    companion object {
        const val A_BUTTON = 1
        const val B_BUTTON = 2
        const val X_BUTTON = 3
        const val Y_BUTTON = 4
        const val START_BUTTON = 5
        const val SELECT_BUTTON = 6
        const val LB_BUTTON = 7
        const val LT_BUTTON = 8
        const val RB_BUTTON = 9
        const val RT_BUTTON = 10
        const val LEFT_ANALOG = 11
        const val LS_BUTTON = 12
        const val RS_BUTTON = 13

        var virtualXInputControllerId = -1
    }
}
