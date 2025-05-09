/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tcl.tools.idea.ui.resourcechooser.colorpicker2

import com.google.common.annotations.VisibleForTesting
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.tcl.tools.idea.ui.colorpicker2.ColorPickerListener
import org.jetbrains.annotations.TestOnly
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AttributeSet
import javax.swing.text.PlainDocument
import kotlin.math.roundToInt
import kotlin.properties.Delegates

private const val HORIZONTAL_MARGIN = HORIZONTAL_MARGIN_TO_PICKER_BORDER + 1
private const val PREFERRED_HEIGHT = 55

private const val TEXT_FIELDS_UPDATING_DELAY = 300

private val COLOR_RANGE = 0..255
private val HUE_RANGE = 0..360
private val PERCENT_RANGE = 0..100

enum class AlphaFormat {
  BYTE,
  PERCENTAGE;

  fun next() : AlphaFormat = when (this) {
    BYTE -> PERCENTAGE
    PERCENTAGE -> BYTE
  }
}

enum class ColorFormat {
  RGB,
  HSB;

  fun next() : ColorFormat = when (this) {
    RGB -> HSB
    HSB -> RGB
  }
}

class ColorValuePanel(private val model: ColorPickerModel) : JPanel(GridBagLayout()), DocumentListener,
  ColorPickerListener {

  /**
   * Used to update the color of picker when color text fields are edited.
   */
  @get:TestOnly
  val updateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)

  @get:TestOnly
  val alphaField = ColorValueField()
  private val alphaHexDocument = DigitColorDocument(alphaField, COLOR_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  private val alphaPercentageDocument = DigitColorDocument(alphaField, PERCENT_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  @get:TestOnly
  val hexField = ColorValueField(hex = true)

  private val alphaLabel = ColorLabel()
  private val colorLabel1 = ColorLabel()
  private val colorLabel2 = ColorLabel()
  private val colorLabel3 = ColorLabel()

  @VisibleForTesting
  val alphaButtonPanel = createAlphaLabel(alphaLabel) {
    currentAlphaFormat = currentAlphaFormat.next()
  }

  @VisibleForTesting
  val colorFormatButtonPanel = createFormatLabels(colorLabel1, colorLabel2, colorLabel3) {
    currentColorFormat = currentColorFormat.next()
  }

  @get:TestOnly
  val colorField1 = ColorValueField()
  private val redDocument = DigitColorDocument(colorField1, COLOR_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  private val hueDocument = DigitColorDocument(colorField1, HUE_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  @get:TestOnly
  val colorField2 = ColorValueField()
  private val greenDocument = DigitColorDocument(colorField2, COLOR_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  private val saturationDocument = DigitColorDocument(colorField2, PERCENT_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  @get:TestOnly
  val colorField3 = ColorValueField()
  private val blueDocument = DigitColorDocument(colorField3, COLOR_RANGE).apply { addDocumentListener(this@ColorValuePanel) }
  private val brightnessDocument = DigitColorDocument(colorField3, PERCENT_RANGE).apply { addDocumentListener(this@ColorValuePanel) }

  @VisibleForTesting
  var currentAlphaFormat by Delegates.observable(loadAlphaFormatProperty()) { _, _, newValue ->
    updateAlphaFormat()
    saveAlphaFormatProperty(newValue)
    repaint()
  }

  @VisibleForTesting
  var currentColorFormat by Delegates.observable(loadColorFormatProperty()) { _, _, newValue ->
    updateColorFormat()
    saveColorFormatProperty(newValue)
    repaint()
  }

  init {
    border = JBUI.Borders.empty(0, HORIZONTAL_MARGIN, 5, HORIZONTAL_MARGIN)
    preferredSize = JBUI.size(COLOR_PICKER_WIDTH, PREFERRED_HEIGHT)
    background = PICKER_BACKGROUND_COLOR
    isFocusable = false

    val c = GridBagConstraints()
    c.fill = GridBagConstraints.HORIZONTAL

    c.weightx = 0.12
    c.gridx = 0
    c.gridy = 0
    add(alphaButtonPanel, c)
    c.gridy = 1
    add(alphaField, c)

    c.weightx = 0.36
    c.gridwidth = 3
    c.gridx = 1
    c.gridy = 0
    add(colorFormatButtonPanel, c)

    c.gridwidth = 1
    c.weightx = 0.12
    c.gridx = 1
    c.gridy = 1
    add(colorField1, c)
    c.gridx = 2
    c.gridy = 1
    add(colorField2, c)
    c.gridx = 3
    c.gridy = 1
    add(colorField3, c)

    // Hex should be longer
    c.gridheight = 1
    c.weightx = 0.51
    c.gridx = 4
    c.gridy = 0
    add(ColorLabel("Hex"), c)
    c.gridy = 1
    add(hexField, c)
    hexField.document = HexColorDocument(hexField)
    hexField.document.addDocumentListener(this)

    updateAlphaFormat()
    updateColorFormat()
    hexField.text = model.hex

    model.addListener(this)
  }

  override fun requestFocusInWindow() = alphaField.requestFocusInWindow()

  private fun updateAlphaFormat() {
    when (currentAlphaFormat) {
      AlphaFormat.BYTE -> {
        alphaLabel.text = "A"
        alphaField.document = alphaHexDocument
        alphaField.text = model.alpha.toString()
      }
      AlphaFormat.PERCENTAGE -> {
        alphaLabel.text = "A%"
        alphaField.document = alphaPercentageDocument
        alphaField.text = (model.alpha * 100f / 0xFF).roundToInt().toString()
      }
    }
    // change the text in document trigger the listener, but it doesn't to update the color in Model in this case.
    updateAlarm.cancelAllRequests()
    repaint()
  }

  private fun updateColorFormat() {
    when (currentColorFormat) {
      ColorFormat.RGB -> {
        colorLabel1.text = "R"
        colorLabel2.text = "G"
        colorLabel3.text = "B"

        colorField1.document = redDocument
        colorField2.document = greenDocument
        colorField3.document = blueDocument

        colorField1.text = model.red.toString()
        colorField2.text = model.green.toString()
        colorField3.text = model.blue.toString()
      }
      ColorFormat.HSB -> {
        colorLabel1.text = "H°"
        colorLabel2.text = "S%"
        colorLabel3.text = "B%"

        colorField1.document = hueDocument
        colorField2.document = saturationDocument
        colorField3.document = brightnessDocument

        colorField1.text = (model.hue * 360).roundToInt().toString()
        colorField2.text = (model.saturation * 100).roundToInt().toString()
        colorField3.text = (model.brightness * 100).roundToInt().toString()
      }
    }
    // change the text in document trigger the listener, but it doesn't to update the color in Model in this case.
    updateAlarm.cancelAllRequests()
    repaint()
  }

  override fun pickingColorChanged(color: Color, source: Any?) = colorChanged(color, source)

  override fun colorChanged(color: Color, source: Any?) = updateTextField(color, source)

  private fun updateTextField(color: Color, source: Any?) {
    if (currentAlphaFormat == AlphaFormat.BYTE) {
      alphaField.setTextIfNeeded(color.alpha.toString(), source)
    }
    else {
      alphaField.setTextIfNeeded((color.alpha * 100f / 0xFF).roundToInt().toString(), source)
    }
    if (currentColorFormat == ColorFormat.RGB) {
      colorField1.setTextIfNeeded(color.red.toString(), source)
      colorField2.setTextIfNeeded(color.green.toString(), source)
      colorField3.setTextIfNeeded(color.blue.toString(), source)
    }
    else {
      val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
      colorField1.setTextIfNeeded((hsb[0] * 360).roundToInt().toString(), source)
      colorField2.setTextIfNeeded((hsb[1] * 100).roundToInt().toString(), source)
      colorField3.setTextIfNeeded((hsb[2] * 100).roundToInt().toString(), source)
    }
    hexField.setTextIfNeeded(String.format("%08X", color.rgb), source)
    // Cleanup the update requests which triggered by setting text in this function
    updateAlarm.cancelAllRequests()
  }

  private fun JTextField.setTextIfNeeded(newText: String?, source: Any?) {
    if (text != newText && (source != this@ColorValuePanel || !isFocusOwner)) {
      text = newText
    }
  }

  override fun insertUpdate(e: DocumentEvent) = update((e.document as ColorDocument).src)

  override fun removeUpdate(e: DocumentEvent) = update((e.document as ColorDocument).src)

  override fun changedUpdate(e: DocumentEvent) = Unit

  private fun update(src: JTextField) {
    updateAlarm.cancelAllRequests()
    updateAlarm.addRequest({ updateColorToColorModel(src) }, TEXT_FIELDS_UPDATING_DELAY)
  }

  private fun updateColorToColorModel(src: JTextField?) {
    val color = if (src == hexField) {
      convertHexToColor(hexField.text)
    }
    else {
      val a = if (currentAlphaFormat == AlphaFormat.BYTE) alphaField.colorValue else (alphaField.colorValue * 0xFF / 100f).roundToInt()
      when (currentColorFormat) {
        ColorFormat.RGB -> {
          val r = colorField1.colorValue
          val g = colorField2.colorValue
          val b = colorField3.colorValue
          Color(r, g, b, a)
        }
        ColorFormat.HSB -> {
          val h = colorField1.colorValue / 360f
          val s = colorField2.colorValue / 100f
          val b = colorField3.colorValue / 100f
          Color((a shl 24) or (0x00FFFFFF and Color.HSBtoRGB(h, s, b)), true)
        }
      }
    }
    model.setColor(color, this)
  }

  companion object {
    private fun createAlphaLabel(alphaLabel: ColorLabel, onClick: () -> Unit) = object : ButtonPanel() {

      init {
        layout = GridLayout(1, 1)
        add(alphaLabel)
      }

      override fun clicked() {
        onClick.invoke()
      }
    }

    private fun createFormatLabels(label1: ColorLabel,
                                   label2: ColorLabel,
                                   label3: ColorLabel,
                                   onClick: () -> Unit) = object : ButtonPanel() {

      init {
        layout = GridLayout(1, 3)
        add(label1)
        add(label2)
        add(label3)
      }

      override fun clicked() {
        onClick.invoke()
      }
    }
  }
}


private val HOVER_BORDER_STROKE = BasicStroke(1f)
private val HOVER_BORDER_COLOR = Color.GRAY.brighter()

private val PRESSED_BORDER_STROKE = BasicStroke(1.2f)
private val PRESSED_BORDER_COLOR = Color.GRAY

private const val ACTION_PRESS_BUTTON_PANEL = "pressButtonPanel"
private const val ACTION_RELEASE_BUTTON_PANEL = "releaseButtonPanel"

@VisibleForTesting
abstract class ButtonPanel : JPanel() {

  private val hoverBorderLeft = JBUI.scale(0)
  private val hoverBorderTop = JBUI.scale(0)
  private val hoverBorderThickness = JBUI.scale(1)
  private val pressedBorderLeft = JBUI.scale(1)
  private val pressedBorderTop = JBUI.scale(1)
  private val pressedBorderThickness = JBUI.scale(2)
  private val borderCornerArc = JBUI.scale(7)

  companion object {
    private enum class Status { NORMAL, HOVER, PRESSED }
  }

  private var mouseStatus by Delegates.observable(Status.NORMAL) { _, _, _ ->
    repaint()
  }

  private val mouseAdapter = object : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent?) = clicked()

    override fun mouseEntered(e: MouseEvent?) {
      if (!isFocusOwner) {
        mouseStatus = Status.HOVER
      }
    }

    override fun mouseExited(e: MouseEvent?) {
      if (!isFocusOwner) {
        mouseStatus = Status.NORMAL
      }
    }

    override fun mousePressed(e: MouseEvent?) {
      if (!isFocusOwner) {
        mouseStatus = Status.PRESSED
      }
    }

    override fun mouseReleased(e: MouseEvent?) {
      if (!isFocusOwner) {
        mouseStatus = if (mouseStatus == Status.PRESSED) Status.HOVER else Status.NORMAL
      }
    }
  }

  private val focusAdapter = object : FocusAdapter() {

    override fun focusGained(e: FocusEvent?) {
      mouseStatus = Status.HOVER
    }

    override fun focusLost(e: FocusEvent?) {
      mouseStatus = Status.NORMAL
    }
  }

  init {
    border = BorderFactory.createEmptyBorder()
    background = PICKER_BACKGROUND_COLOR
    addMouseListener(mouseAdapter)
    addFocusListener(focusAdapter)

    with (getInputMap(JComponent.WHEN_FOCUSED)) {
      put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), ACTION_PRESS_BUTTON_PANEL)
      put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), ACTION_RELEASE_BUTTON_PANEL)
    }

    with (actionMap) {
      put(ACTION_PRESS_BUTTON_PANEL, object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
          mouseStatus = Status.PRESSED
        }
      })

      put(ACTION_RELEASE_BUTTON_PANEL, object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
          mouseStatus = Status.HOVER
          clicked()
        }
      })
    }
  }

  // Needs to be final to be used in init block
  final override fun addMouseListener(l: MouseListener?) = super.addMouseListener(l)

  // Needs to be final to be used in init block
  final override fun addFocusListener(l: FocusListener?) = super.addFocusListener(l)

  override fun isFocusable() = true

  abstract fun clicked()

  override fun paintBorder(g: Graphics) {
    if (g !is Graphics2D) {
      return
    }
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val originalStroke = g.stroke
    when (mouseStatus) {
      Status.HOVER -> {
        g.stroke = HOVER_BORDER_STROKE
        g.color = HOVER_BORDER_COLOR
        g.drawRoundRect(hoverBorderLeft, hoverBorderTop,
                        width - hoverBorderThickness, height - hoverBorderThickness,
                        borderCornerArc, borderCornerArc)
      }
      Status.PRESSED -> {
        g.stroke = PRESSED_BORDER_STROKE
        g.color = PRESSED_BORDER_COLOR
        g.drawRoundRect(pressedBorderLeft, pressedBorderTop,
                        width - pressedBorderThickness, height - pressedBorderThickness,
                        borderCornerArc, borderCornerArc)
      }
      else -> return
    }
    g.stroke = originalStroke
  }
}

private class ColorLabel(text: String = ""): JLabel(text, SwingConstants.CENTER) {
  init {
    foreground = PICKER_TEXT_COLOR
  }
}

private const val ACTION_UP = "up"
private const val ACTION_DOWN = "down"

@VisibleForTesting
class ColorValueField(private val hex: Boolean = false): JTextField(if (hex) 8 else 3) {

  init {
    horizontalAlignment = CENTER
    isEnabled = true
    isEditable = true

    val labelFont = UIUtil.getLabelFont()
    font = labelFont.deriveFont(labelFont.size * 0.9f)

    addFocusListener(object : FocusAdapter() {
      override fun focusGained(e: FocusEvent?) {
        selectAll()
      }

      override fun focusLost(e: FocusEvent?) {
        val size = document?.length ?: return
        selectionStart = size
        selectionEnd = size
      }
    })
    if (!hex) {
      // Don't increase value for hex field.
      with(getInputMap(JComponent.WHEN_FOCUSED)) {
        put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), ACTION_UP)
        put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), ACTION_DOWN)
      }
      with(actionMap) {
        put(ACTION_UP, object : AbstractAction() {
          override fun actionPerformed(e: ActionEvent) = increaseValue(1)
        })
        put(ACTION_DOWN, object : AbstractAction() {
          override fun actionPerformed(e: ActionEvent) = increaseValue(-1)
        })
      }
    }
  }

  private fun increaseValue(diff: Int) {
    assert(!hex)

    val doc = document as DigitColorDocument
    val newValue = doc.getText(0, doc.length).toInt() + diff
    val valueInRange = Math.max(doc.valueRange.start, Math.min(newValue, doc.valueRange.endInclusive))
    text = valueInRange.toString()
  }

  override fun isFocusable() = true

  val colorValue: Int
    get() {
      val rawText = text
      return if (rawText.isBlank()) 0 else Integer.parseInt(rawText, if (hex) 16 else 10)
    }
}

private abstract class ColorDocument(internal val src: JTextField) : PlainDocument() {

  override fun insertString(offs: Int, str: String, a: AttributeSet?) {
    val source = str.toCharArray()
    val selected = src.selectionEnd - src.selectionStart
    val newLen = src.text.length - selected + str.length
    if (newLen > src.columns) {
      return
    }

    val charsToInsert = source
      .filter { isLegalCharacter(it) }
      .map { it.toUpperCase() }
      .joinToString("")

    val res = StringBuilder(src.text).insert(offs, charsToInsert).toString()
    if (!isLegalValue(res)) {
      return
    }
    super.insertString(offs, charsToInsert, a)
  }

  abstract fun isLegalCharacter(c: Char): Boolean

  abstract fun isLegalValue(str: String): Boolean
}

private class DigitColorDocument(src: JTextField, val valueRange: IntRange) : ColorDocument(src) {

  override fun isLegalCharacter(c: Char) = c.isDigit()

  override fun isLegalValue(str: String) = try { str.toInt() in valueRange } catch (_: NumberFormatException) { false }
}

private class HexColorDocument(src: JTextField) : ColorDocument(src) {

  override fun isLegalCharacter(c: Char) = StringUtil.isHexDigit(c)

  override fun isLegalValue(str: String) = true
}

private fun convertHexToColor(hex: String): Color {
  val s = if (hex == "") "0" else hex
  val i = s.toLong(16)
  val a = if (hex.length > 6) i shr 24 and 0xFF else 0xFF
  val r = i shr 16 and 0xFF
  val g = i shr 8 and 0xFF
  val b = i and 0xFF
  return Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())
}

private const val PROPERTY_PREFIX = "colorValuePanel_"

private const val PROPERTY_NAME_ALPHA_FORMAT = PROPERTY_PREFIX + "alphaFormat"
private val DEFAULT_ALPHA_FORMAT = AlphaFormat.PERCENTAGE

private const val PROPERTY_NAME_COLOR_FORMAT = PROPERTY_PREFIX + "colorFormat"
private val DEFAULT_COLOR_FORMAT = ColorFormat.RGB

private fun loadAlphaFormatProperty(): AlphaFormat {
  val alphaFormatName = PropertiesComponent.getInstance().getValue(PROPERTY_NAME_ALPHA_FORMAT, DEFAULT_ALPHA_FORMAT.name)
  return try {
    AlphaFormat.valueOf(alphaFormatName)
  }
  catch (e: IllegalArgumentException) {
    DEFAULT_ALPHA_FORMAT
  }
}

private fun saveAlphaFormatProperty(alphaFormat: AlphaFormat) {
  PropertiesComponent.getInstance().setValue(PROPERTY_NAME_ALPHA_FORMAT, alphaFormat.name)
}

private fun loadColorFormatProperty(): ColorFormat {
  val colorFormatName = PropertiesComponent.getInstance().getValue(PROPERTY_NAME_COLOR_FORMAT, DEFAULT_COLOR_FORMAT.name)
  return try {
    ColorFormat.valueOf(colorFormatName)
  }
  catch (e: IllegalArgumentException) {
    DEFAULT_COLOR_FORMAT
  }
}

private fun saveColorFormatProperty(colorFormat: ColorFormat) {
  PropertiesComponent.getInstance().setValue(PROPERTY_NAME_COLOR_FORMAT, colorFormat.name)
}
