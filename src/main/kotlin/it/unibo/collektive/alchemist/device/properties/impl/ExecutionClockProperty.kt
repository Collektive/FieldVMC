package it.unibo.collektive.alchemist.device.properties.impl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.collektive.alchemist.device.properties.Clock
import it.unibo.collektive.alchemist.device.properties.Cycle.SPAWNING
import it.unibo.collektive.alchemist.device.properties.ExecutionClock

/**
 * An Alchemist [NodeProperty] that manages the execution clock state for a node.
 *
 * @param T The concentration type of the node.
 * @param P The position type of the environment.
 * @property node The Alchemist node this property is attached to.
 * @param environment The environment in which the node exists.
 */
class ExecutionClockProperty<T, P : Position<P>>(
    override val node: Node<T>,
    private val environment: Environment<T, P>,
) : ExecutionClock,
    NodeProperty<T> {
    private var clock: Clock = Clock()

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = ExecutionClockProperty(node, environment)

    override fun currentClock(): Clock = clock

    override fun nextClock() {
        clock = clock.next()
    }

    override fun justSpawned(time: Int) {
        clock = Clock(time = time, action = SPAWNING)
    }

    override fun toString(): String = clock.toString()
}
