/*
 * Copyright (c) 2023, tuanchauict
 */

package mono.html.toolbar.view.shapetool2.viewdata

import mono.actionmanager.RetainableActionType
import mono.html.toolbar.view.shapetool2.CloudItemSelectionState
import mono.livedata.LiveData
import mono.livedata.combineLiveData
import mono.livedata.map
import mono.shape.ShapeExtraManager
import mono.shape.extra.RectangleExtra
import mono.shape.extra.style.StraightStrokeDashPattern
import mono.shape.shape.AbstractShape
import mono.shape.shape.Group
import mono.shape.shape.Line
import mono.shape.shape.MockShape
import mono.shape.shape.Rectangle
import mono.shape.shape.Text

/**
 * Data controller class of Rectangle appearance
 */
internal class RectangleAppearanceDataController(
    shapesLiveData: LiveData<Set<AbstractShape>>,
    retainableActionLiveData: LiveData<RetainableActionType>
) {
    private val singleRectExtraLiveData: LiveData<RectangleExtra?> = shapesLiveData.map {
        when (val line = it.singleOrNull()) {
            is Rectangle -> line.extra
            is Text -> line.extra.boundExtra
            is Line,
            is MockShape,
            is Group,
            null -> null
        }
    }

    private val defaultRectangleExtraLiveData: LiveData<RectangleExtra?> =
        retainableActionLiveData.map {
            when (it) {
                RetainableActionType.ADD_RECTANGLE,
                RetainableActionType.ADD_TEXT -> ShapeExtraManager.defaultRectangleExtra

                RetainableActionType.IDLE,
                RetainableActionType.ADD_LINE -> null
            }
        }

    val fillToolStateLiveData: LiveData<CloudItemSelectionState?> =
        createFillAppearanceVisibilityLiveData()
    val borderToolStateLiveData: LiveData<CloudItemSelectionState?> =
        createBorderAppearanceVisibilityLiveData()
    val borderDashPatternLiveData: LiveData<StraightStrokeDashPattern?> =
        createBorderDashPatternLiveData()
    val borderRoundedCornerLiveData: LiveData<Boolean?> =
        createBorderRoundedCornerLiveData()

    val hasAnyVisibleToolLiveData: LiveData<Boolean> = combineLiveData(
        fillToolStateLiveData,
        borderToolStateLiveData,
        borderDashPatternLiveData,
        borderRoundedCornerLiveData
    ) { list -> list.any { it != null } }

    private fun createFillAppearanceVisibilityLiveData(): LiveData<CloudItemSelectionState?> {
        val selectedLiveData =
            singleRectExtraLiveData.map { it?.toFillAppearanceVisibilityState() }
        val defaultLiveData =
            defaultRectangleExtraLiveData.map { it?.toFillAppearanceVisibilityState() }
        return selectedOrDefault(selectedLiveData, defaultLiveData)
    }

    private fun createBorderAppearanceVisibilityLiveData(): LiveData<CloudItemSelectionState?> =
        selectedOrDefault(
            selectedLiveData = singleRectExtraLiveData.map { it?.toBorderState() },
            defaultLiveData = defaultRectangleExtraLiveData.map { it?.toBorderState() }
        )

    private fun createBorderDashPatternLiveData(): LiveData<StraightStrokeDashPattern?> =
        selectedOrDefault(
            selectedLiveData = singleRectExtraLiveData.map { it?.dashPattern },
            defaultLiveData = defaultRectangleExtraLiveData.map { it?.dashPattern }
        )

    private fun createBorderRoundedCornerLiveData(): LiveData<Boolean?> {
        val selectedLiveData = singleRectExtraLiveData
            .map { it?.takeIf { it.isBorderEnabled } }
            .map { it?.isRoundedCorner }
        val defaultLiveData = defaultRectangleExtraLiveData
            .map { it?.takeIf { it.isBorderEnabled } }
            .map { it?.isRoundedCorner }
        return selectedOrDefault(selectedLiveData, defaultLiveData)
    }

    private fun RectangleExtra.toFillAppearanceVisibilityState(): CloudItemSelectionState =
        CloudItemSelectionState(isFillEnabled, userSelectedFillStyle.id)

    private fun RectangleExtra.toBorderState(): CloudItemSelectionState =
        CloudItemSelectionState(isBorderEnabled, userSelectedBorderStyle.id)

    private fun <T> selectedOrDefault(
        selectedLiveData: LiveData<T?>,
        defaultLiveData: LiveData<T?>
    ): LiveData<T?> = combineLiveData(selectedLiveData, defaultLiveData) { s, d -> s ?: d }
}
