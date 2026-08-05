package it.unibo.collektive.alchemist.device.sensors.impl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor

/**
 * An Alchemist [NodeProperty] that provides the node with the capability to sense resources.
 *
 * @param T The concentration type of the node.
 * @param P The position type of the environment.
 * @param environment The simulation environment.
 * @property node The Alchemist node this property is attached to.
 * @property resourceLowerBound The lower bound threshold for the resource.
 * @property maxResource The maximum capacity or upper bound for the resource.
 */
class ResourceSensorProperty<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    override val node: Node<T>,
    override val resourceLowerBound: Double,
    override val maxResource: Double,
) : ResourceSensor,
    NodeProperty<T> {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> =
        ResourceSensorProperty(environment, node, resourceLowerBound, maxResource)

    override fun getResource(): Double =
        when (val layerValue = environment.getLayer(localResource)?.getValue(environment.getPosition(node))) {
            is Number -> layerValue.toDouble()
            else -> error("ResourceSensorProperty: $layerValue is not a number")
        }

    @Suppress("UNCHECKED_CAST")
    override fun setCurrentOverallResource(resource: Double) = node.setConcentration(resourceMolecule, resource as T)

    /**
     * Companion object containing the specific molecules used for tracking resources.
     */
    companion object {
        private val resourceMolecule = SimpleMolecule("resource")
        private val localResource = SimpleMolecule("localResource")
    }
}
