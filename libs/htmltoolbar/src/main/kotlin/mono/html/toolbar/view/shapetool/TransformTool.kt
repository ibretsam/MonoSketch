@file:Suppress("FunctionName", "ktlint:filename")

package mono.html.toolbar.view.shapetool

import mono.actionmanager.OneTimeActionType
import mono.graphics.geo.Rect
import mono.html.Div
import mono.html.Input
import mono.html.InputType
import mono.html.Span
import mono.html.appendElement
import mono.html.setAttributes
import mono.html.setOnChangeListener
import mono.lifecycle.LifecycleOwner
import mono.livedata.LiveData
import mono.livedata.map
import mono.shape.shape.AbstractShape
import mono.shape.shape.Rectangle
import mono.shape.shape.Text
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement

internal class TransformToolViewController(
    lifecycleOwner: LifecycleOwner,
    container: Element,
    singleShapeLiveData: LiveData<AbstractShape?>,
    setOneTimeAction: (OneTimeActionType) -> Unit
) {
    private val xInput = NumberCellInput(0) {
        setOneTimeAction(OneTimeActionType.ChangeShapeBound(newLeft = it))
    }
    private val yInput = NumberCellInput(0) {
        setOneTimeAction(OneTimeActionType.ChangeShapeBound(newTop = it))
    }
    private val wInput = NumberCellInput(10, 1) {
        setOneTimeAction(OneTimeActionType.ChangeShapeBound(newWidth = it))
    }
    private val hInput = NumberCellInput(10, 1) {
        setOneTimeAction(OneTimeActionType.ChangeShapeBound(newHeight = it))
    }

    val visibilityStateLiveData: LiveData<Boolean>

    init {
        val section = container.Section("TRANSFORM") {
            Div("transform-grid") {
                NumberCell("X", xInput)
                NumberCell("W", wInput)
                NumberCell("Y", yInput)
                NumberCell("H", hInput)
            }
        }

        visibilityStateLiveData = singleShapeLiveData.map { it != null }

        singleShapeLiveData.observe(lifecycleOwner) {
            val isSizeChangeable = it is Rectangle || it is Text
            section.bindClass(CssClass.HIDE, it == null)

            setEnabled(it != null, isSizeChangeable)
            if (it != null) {
                setValue(it.bound)
            }
        }
    }

    private fun setValue(bound: Rect) {
        xInput.value = bound.left.toString()
        yInput.value = bound.top.toString()
        wInput.value = bound.width.toString()
        hInput.value = bound.height.toString()
    }

    private fun setEnabled(isPositionEnabled: Boolean, isSizeEnabled: Boolean) {
        xInput.disabled = !isPositionEnabled
        yInput.disabled = !isPositionEnabled
        wInput.disabled = !isSizeEnabled
        hInput.disabled = !isSizeEnabled
    }
}

private fun Element.NumberCell(
    title: String,
    inputElement: Element
) {
    Div("cell") {
        Span("tool-title", text = title)
        appendElement(inputElement)
    }
}

private fun NumberCellInput(
    value: Int,
    minValue: Int? = null,
    onValueChange: (Int) -> Unit
): HTMLInputElement = Input(null, InputType.NUMBER, classes = "tool-input-text") {
    if (minValue != null) {
        setAttributes("min" to minValue)
    }
    this.value = value.toString()

    setOnChangeListener {
        onValueChange(this.value.toInt())
    }
}
