package it.unibo.collektive.alchemist.device.sensors.impl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.collektive.alchemist.device.sensors.LocationSensor

/**
 * An Alchemist [NodeProperty] that provides the node with its current location
 * and the locations of its neighboring nodes.
 *
 * @param T The concentration type of the node.
 * @param P The position type of the environment.
 * @param environment The simulation environment.
 * @property node The Alchemist node this property is attached to.
 */
class LocationSensorProperty<T : Any, P : Position<P>>(
    private val environment: Environment<T, P>,
    override val node: Node<T>,
) : LocationSensor,
    NodeProperty<T> {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = LocationSensorProperty(environment, node)

    override fun coordinates(): Pair<Double, Double> {
        val position = environment.getPosition(node).coordinates
        return Pair(position[0], position[1])
    }

    override fun surroundings(): List<Pair<Double, Double>> =
        environment.getNeighborhood(node).map { node ->
            environment.getPosition(node).coordinates.let { Pair(it[0], it[1]) }
        }
}
