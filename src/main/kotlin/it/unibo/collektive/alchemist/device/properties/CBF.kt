package it.unibo.collektive.alchemist.device.properties

import it.unibo.collektive.model.Position

/**
 * Represents a Control Barrier Function (CBF) used to evaluate the safety of a given point.
 */
interface CBF {
    /**
     * Returns a value greater or equal to 0 if the point is safe, otherwise returns a negative value.
     */
    fun isSafe(position: Position): Double
}
