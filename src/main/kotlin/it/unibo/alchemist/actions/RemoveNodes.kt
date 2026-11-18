package it.unibo.alchemist.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractNodeAction

/**
 * An Alchemist action that removes the specified [node] from the [environment] if its position
 * falls within a specific rectangular removal zone.
 *
 * The removal zone is horizontally centered within a given [width], with its total horizontal
 * span defined by the [killingRange]. Vertically, the zone extends from the [origin] to the [height].
 * If the node's coordinates are inside these bounds during execution, it is removed.
 */
class RemoveNodes<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    node: Node<T>,
    reaction: Reaction<T>,
    private val killingRange: Double,
    private val origin: Double,
    private val width: Double,
    private val height: Double,
) : AbstractNodeAction<T>(reaction, node) {
    override fun cloneTargeting(newReaction: Reaction<T>, newTarget: Node<T>): Action<T> =
        RemoveNodes(environment, newTarget, newReaction, killingRange, origin, width, height)

    override fun execute() {
        val nodePosition = environment.currentPositionOf(targetNode).coordinates
        val start = (width / 2) - (killingRange / 2) to origin
        val end = (width / 2) + (killingRange / 2) to height
        if (nodePosition[0] in start.first..end.first && nodePosition[1] in start.second..end.second) {
            environment.simulation.schedule { environment.removeNode(targetNode) }
        }
    }
}
