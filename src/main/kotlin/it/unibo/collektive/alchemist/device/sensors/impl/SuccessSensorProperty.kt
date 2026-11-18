@file:Suppress("UNCHECKED_CAST")

package it.unibo.collektive.alchemist.device.sensors.impl

import it.unibo.alchemist.model.Capability
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor

/**
 * An Alchemist [NodeProperty] that provides the node with the capability to track and manage success metrics.
 *
 * @param T The concentration type of the node.
 * @param P The position type of the environment.
 * @param maxSuccess The maximum possible success value.
 * @property node The Alchemist node this property is attached to.
 * @param environment The simulation environment.
 */
class SuccessSensorProperty<T, P : Position<P>>(
    private val maxSuccess: Double,
    override val node: Node<T>,
    private val environment: Environment<T, P>,
) : SuccessSensor,
    Capability<T> {
    override fun cloneOnNewNode(node: Node<T>): Capability<T> = SuccessSensorProperty(maxSuccess, node, environment)

    @Suppress("UNCHECKED_CAST")
    override fun setSuccess(success: Double) {
        node[SimpleMolecule("success")] = success as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun setLocalSuccess(localSuccess: Double) {
        node[SimpleMolecule("local-success")] = localSuccess as T
    }

    override fun getSuccess(): Double = node[SimpleMolecule("success")].getOrNull() as Double

    override fun getLocalSuccess(): Double = getFromLayer("successSource") // ?: random.nextDouble(0.0, maxSuccess)

    private fun <T> getFromLayer(name: String): T = (
        environment.layerFor(SimpleMolecule(name))?.currentValueAt(environment.currentPositionOf(node))
            ?: IllegalStateException("Layer $name not found")
        ) as T

    override fun toString(): String = this::class.simpleName ?: javaClass.name
}
