/*
 * Copyright (c) 2023, tuanchauict
 */

package mono.state

import mono.common.setTimeout
import mono.environment.Build
import mono.graphics.geo.Point
import mono.html.canvas.CanvasViewController
import mono.lifecycle.LifecycleOwner
import mono.livedata.combineLiveData
import mono.shape.serialization.SerializableGroup
import mono.shape.serialization.ShapeSerializationUtil
import mono.shape.shape.RootGroup
import mono.state.command.CommandEnvironment
import mono.store.manager.StorageDocument
import mono.store.manager.StoreKeys.LAST_OPEN
import mono.store.manager.StoreKeys.OBJECT_CONTENT
import mono.store.manager.StoreKeys.OBJECT_OFFSET
import mono.store.manager.StoreKeys.WORKSPACE
import mono.uuid.UUID

/**
 * A class which manages state history of the shapes.
 */
internal class StateHistoryManager(
    private val lifecycleOwner: LifecycleOwner,
    private val environment: CommandEnvironment,
    private val canvasViewController: CanvasViewController,
    private val workspaceDocument: StorageDocument = StorageDocument.get(WORKSPACE)
) {
    private val historyStack = HistoryStack()

    fun restoreAndStartObserveStateChange(rootId: String) {
        val adjustedRootId = rootId.ifEmpty {
            workspaceDocument.get(LAST_OPEN) ?: UUID.generate()
        }

        restoreShapes(adjustedRootId)
        restoreOffset(adjustedRootId)

        workspaceDocument.set(LAST_OPEN, adjustedRootId)

        combineLiveData(
            environment.shapeManager.versionLiveData,
            environment.editingModeLiveData
        ) { versionCode, editingMode ->
            if (!editingMode.isEditing && versionCode != editingMode.skippedVersion) {
                registerBackupShapes(versionCode)
            }
        }

        canvasViewController.drawingOffsetPointPxLiveData.observe(lifecycleOwner) {
            workspaceDocument.childDocument(environment.shapeManager.root.id)
                .set(OBJECT_OFFSET, "${it.left}|${it.top}")
        }
    }

    fun clear() = historyStack.clear()

    fun undo() {
        val history = historyStack.undo() ?: return
        val root = RootGroup(history.serializableGroup)
        environment.replaceRoot(root)
    }

    fun redo() {
        val history = historyStack.redo() ?: return
        val root = RootGroup(history.serializableGroup)
        environment.replaceRoot(root)
    }

    private fun registerBackupShapes(version: Int) {
        setTimeout(300) {
            // Only backup if the shape manager is idle.
            if (environment.shapeManager.versionLiveData.value == version) {
                backupShapes()
            }
        }
    }

    private fun backupShapes() {
        val root = environment.shapeManager.root
        val serializableGroup = root.toSerializableShape(true) as SerializableGroup

        historyStack.pushState(root.versionCode, serializableGroup)

        val jsonRoot = ShapeSerializationUtil.toJson(serializableGroup)
        workspaceDocument.childDocument(root.id).set(OBJECT_CONTENT, jsonRoot)
    }

    private fun restoreShapes(rootId: String = "") {
        val rootJson = workspaceDocument.childDocument(rootId).get(OBJECT_CONTENT)
        val serializableGroup =
            rootJson?.let(ShapeSerializationUtil::fromJson) as? SerializableGroup
        val rootGroup = if (serializableGroup != null) {
            RootGroup(serializableGroup)
        } else {
            RootGroup(rootId)
        }
        environment.replaceRoot(rootGroup)
    }

    private fun restoreOffset(rootId: String = "") {
        workspaceDocument.childDocument(rootId).get(OBJECT_OFFSET)
        val storedOffsetString =
            workspaceDocument.childDocument(rootId).get(OBJECT_OFFSET) ?: return
        val (leftString, topString) = storedOffsetString.split('|').takeIf { it.size == 2 }
            ?: return
        val left = leftString.toIntOrNull() ?: return
        val top = topString.toIntOrNull() ?: return
        val offset = Point(left, top)
        canvasViewController.setOffset(offset)
    }

    private class HistoryStack {
        private val undoStack = mutableListOf<History>()
        private val redoStack = mutableListOf<History>()

        fun pushState(version: Int, state: SerializableGroup) {
            if (version == undoStack.lastOrNull()?.versionCode) {
                return
            }
            undoStack.add(History(version, state))
            redoStack.clear()
            if (Build.DEBUG) {
                println("Push history stack ${undoStack.map { it.versionCode }}")
            }
        }

        fun clear() {
            undoStack.clear()
            redoStack.clear()
        }

        fun undo(): History? {
            if (undoStack.size <= 1) {
                return null
            }
            val currentState = undoStack.removeLastOrNull()
            if (currentState != null) {
                redoStack.add(currentState)
            }
            return undoStack.lastOrNull()
        }

        fun redo(): History? {
            val currentState = redoStack.removeLastOrNull()
            if (currentState != null) {
                undoStack.add(currentState)
            }
            return currentState
        }
    }

    private class History(val versionCode: Int, val serializableGroup: SerializableGroup)
}
