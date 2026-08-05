package it.unibo.collektive.alchemist.device.sensors

import it.unibo.collektive.model.Position

/**
 * Interface representing a sensor capable of retrieving the location of the node and its neighbors.
 */
interface LocationSensor {
    /**
     * Returns the coordinates of the node's position inside the environment.
     */
    fun coordinates(): Position
}
