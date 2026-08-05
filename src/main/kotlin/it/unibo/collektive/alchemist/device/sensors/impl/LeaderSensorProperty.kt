package it.unibo.collektive.alchemist.device.sensors.impl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.alchemist.device.sensors.LeaderSensor

/**
 * An Alchemist [NodeProperty] that provides the node with the capability to sense and act as a leader.
 *
 * @param T The concentration type of the node.
 * @param P The position type of the environment.
 * @property leaderRadius The radius within which the node acts as a leader.
 * @param environment The simulation environment.
 * @property node The Alchemist node this property is attached to.
 */
class LeaderSensorProperty<T, P : Position<P>>(
    override val leaderRadius: Double,
    private val environment: Environment<T, P>,
    override val node: Node<T>,
) : LeaderSensor,
    NodeProperty<T> {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = LeaderSensorProperty(leaderRadius, environment, node)

    override fun isLeader(): Boolean {
        val leaderMolecule = SimpleMolecule("leader")
        return if (node.contains(leaderMolecule)) node.getConcentration(leaderMolecule) as Boolean else false
    }

    @Suppress("UNCHECKED_CAST")
    override fun setLeader(leader: Boolean) {
        node.setConcentration(SimpleMolecule("leader"), leader as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <ID : Any> setLeaderId(id: ID) = node.setConcentration(SimpleMolecule("leaderId"), id as T)
}
