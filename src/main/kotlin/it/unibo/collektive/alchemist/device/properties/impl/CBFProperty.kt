package it.unibo.collektive.alchemist.device.properties.impl

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.collektive.alchemist.device.properties.CBF
import it.unibo.common.sdf.SDF

/**
 * An Alchemist [NodeProperty] that provides Control Barrier Function (CBF) capabilities.
 *
 * It evaluates the safety of a given point using a Signed Distance Field (SDF).
 *
 * @param T The concentration type of the node.
 * @property node The Alchemist node this property is attached to.
 * @param sdf The Signed Distance Field used to compute the safety value.
 */
class CBFProperty<T>(
    override val node: Node<T>,
    private val sdf: SDF,
) : CBF,
    NodeProperty<T> {
    override fun isSafe(p: Pair<Double, Double>): Double = sdf(p)

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = CBFProperty(node, sdf)
}
