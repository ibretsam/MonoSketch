package mono.shape.command

import mono.graphics.geo.Rect
import mono.shape.ShapeManager
import mono.shape.shape.AbstractShape
import mono.shape.shape.Group

class ChangeBound(private val target: AbstractShape, private val newBound: Rect) : Command() {
    override fun getDirectAffectedParent(shapeManager: ShapeManager): Group? =
        shapeManager.getGroup(target.parentId)

    override fun execute(shapeManager: ShapeManager, parent: Group) {
        val currentVersion = target.version
        target.setBound(newBound)
        if (currentVersion != target.version) {
            parent.update { true }
        }
    }
}

class ChangeExtra(
    private val target: AbstractShape,
    private val extraUpdater: AbstractShape.ExtraUpdater
) : Command() {
    override fun getDirectAffectedParent(shapeManager: ShapeManager): Group? =
        shapeManager.getGroup(target.parentId)

    override fun execute(shapeManager: ShapeManager, parent: Group) {
        val currentVersion = target.version
        target.setExtra(extraUpdater)
        if (currentVersion != target.version) {
            parent.update { true }
        }
    }
}
