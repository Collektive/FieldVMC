package it.unibo.alchemist.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractNodeAction

/**
 * An Alchemist action that moves the specified [node] horizontally by a given [movingRange].
 *
 * Upon execution, the node's current X coordinate is incremented by the [movingRange],
 * while the Y coordinate remains unchanged.
 */
class MoveCluster<T : Any, P : Position<P>>(
    private val environment: Environment<T, P>,
    node: Node<T>,
    reaction: Reaction<T>,
    private val movingRange: Double,
) : AbstractNodeAction<T>(reaction, node) {
    override fun cloneTargeting(newReaction: Reaction<T>, newTarget: Node<T>): Action<T> =
        MoveCluster(environment, newTarget, newReaction, movingRange)

    override fun execute() {
        val nodePosition = environment.currentPositionOf(targetNode).coordinates
        val newPosition = (nodePosition[0] + movingRange) to nodePosition[1]
        environment.moveNodeTo(targetNode, environment.makePosition(newPosition.first, newPosition.second))
    }
}
