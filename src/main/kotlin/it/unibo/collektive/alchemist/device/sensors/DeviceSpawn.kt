package it.unibo.collektive.alchemist.device.sensors

import it.unibo.collektive.model.Position

/**
 * Interface representing the capability of a device to spawn new nodes or destroy itself in the environment.
 */
interface DeviceSpawn {
    /**
     * Spawns a new node in the given [coordinate].
     */
    fun spawn(coordinate: Position): Double

    /**
     * The node will destroy itself.
     */
    fun selfDestroy()

    /**
     * Returns the current time inside the simulation environment.
     */
    fun currentTime(): Double

    /**
     * The fixed distance from the node at which a new spawn occurs.
     */
    val cloningRange: Double

    /**
     * The maximum number of children this node is allowed to spawn.
     */
    val maxChildren: Int

    /**
     * The minimum amount of time to wait before new spawn is allowed.
     */
    val minSpawnWait: Double
}
