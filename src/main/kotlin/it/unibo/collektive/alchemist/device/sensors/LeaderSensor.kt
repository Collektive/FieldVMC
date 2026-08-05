package it.unibo.collektive.alchemist.device.sensors

/**
 * Interface representing a sensor capable of checking and managing the leader status of a node.
 */
interface LeaderSensor {

    /**
     * The radius within which the node acts as a leader.
     */
    val leaderRadius: Double

    /**
     * Checks if the node is considered leader.
     */
    fun isLeader(): Boolean

    /**
     * Set the node as leader.
     */
    fun setLeader(leader: Boolean)

    /**
     * Set the node's leader [id].
     */
    fun <ID : Any> setLeaderId(id: ID)
}
