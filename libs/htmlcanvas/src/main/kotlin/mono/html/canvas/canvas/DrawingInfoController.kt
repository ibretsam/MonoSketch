package mono.html.canvas.canvas

import mono.graphics.geo.Point
import mono.graphics.geo.Rect
import mono.graphics.geo.Size
import mono.graphics.geo.SizeF
import mono.html.Canvas
import mono.html.px
import mono.livedata.LiveData
import mono.livedata.MutableLiveData
import mono.livedata.distinctUntilChange
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.LEFT
import org.w3c.dom.MIDDLE
import kotlin.math.ceil
import kotlin.math.floor

/**
 * A controller class to manage drawing info for the other canvas.
 */
internal class DrawingInfoController(container: HTMLDivElement) {
    private val context: CanvasRenderingContext2D

    private val drawingInfoMutableLiveData: MutableLiveData<DrawingInfo> =
        MutableLiveData(DrawingInfo())
    val drawingInfoLiveData: LiveData<DrawingInfo> =
        drawingInfoMutableLiveData.distinctUntilChange()

    init {
        val canvas = Canvas(container, CLASS_NAME)
        context = canvas.getContext("2d") as CanvasRenderingContext2D

        setSize(canvas.width, canvas.height)
        setFont(13)
    }

    fun setFont(fontSize: Int) {
        val cellSizePx = context.getCellSizePx(fontSize)
        val cellCharOffsetPx = SizeF(0.0, cellSizePx.height * 0.2)
        drawingInfoMutableLiveData.value =
            drawingInfoMutableLiveData.value.copy(
                cellSizePx = cellSizePx,
                cellCharOffsetPx = cellCharOffsetPx,
                font = "normal normal normal ${fontSize.px} $DEFAULT_FONT",
                fontSize = fontSize
            )
    }

    fun setSize(widthPx: Int, heightPx: Int) {
        drawingInfoMutableLiveData.value =
            drawingInfoMutableLiveData.value.copy(canvasSizePx = Size(widthPx, heightPx))
    }

    fun setOffset(offset: Point) {
        drawingInfoMutableLiveData.value = drawingInfoMutableLiveData.value.copy(offsetPx = offset)
    }

    private fun CanvasRenderingContext2D.getCellSizePx(fontSize: Int): SizeF {
        context.font = font
        context.textAlign = CanvasTextAlign.LEFT
        context.textBaseline = CanvasTextBaseline.MIDDLE
        val cWidth = floor(fontSize.toDouble() * 0.63)
        val cHeight = fontSize.toDouble() * 1.312
        return SizeF(cWidth, cHeight)
    }

    internal data class DrawingInfo(
        val offsetPx: Point = Point.ZERO,
        val cellSizePx: SizeF = SizeF(1.0, 1.0),
        val cellCharOffsetPx: SizeF = SizeF(0.0, 0.0),
        val canvasSizePx: Size = Size(1, 1),
        val font: String = "",
        val fontSize: Int = 0
    ) {
        val boundPx: Rect = Rect(offsetPx, canvasSizePx)

        private val boardOffsetRow: Int = (-offsetPx.top / cellSizePx.height).toInt()
        private val boardOffsetColumn: Int = (-offsetPx.left / cellSizePx.width).toInt()
        private val rowCount: Int = ceil(canvasSizePx.height / cellSizePx.height).toInt()
        private val columnCount: Int = ceil(canvasSizePx.width / cellSizePx.width).toInt()

        val boardBound: Rect = Rect.byLTWH(boardOffsetColumn, boardOffsetRow, columnCount, rowCount)

        internal val boardRowRange: IntRange = boardOffsetRow..(boardOffsetRow + rowCount)
        internal val boardColumnRange: IntRange =
            boardOffsetColumn..(boardOffsetColumn + columnCount)

        fun toXPx(column: Double): Double = floor(offsetPx.left + cellSizePx.width * column)
        fun toYPx(row: Double): Double = floor(offsetPx.top + cellSizePx.height * row)
        fun toBoardRow(yPx: Int): Int = floor((yPx - offsetPx.top) / cellSizePx.height).toInt()
        fun toBoardColumn(xPx: Int): Int = floor((xPx - offsetPx.left) / cellSizePx.width).toInt()
        fun toWidthPx(width: Double) = floor(cellSizePx.width * width)
        fun toHeightPx(height: Double) = floor(cellSizePx.height * height)
    }

    companion object {
        const val DEFAULT_FONT = "'Jetbrains Mono'"
        private const val CLASS_NAME = "drawing-info"
    }
}
