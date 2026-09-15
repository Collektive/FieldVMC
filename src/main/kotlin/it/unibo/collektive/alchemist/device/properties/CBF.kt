package it.unibo.collektive.alchemist.device.properties

import it.unibo.collektive.model.Position

/**
 * Represents a Control Barrier Function (CBF), describing which portion of the space is safe to occupy.
 */
fun interface CBF {
    /**
     * Returns the signed distance between [position] and the closest border of the unsafe space:
     * the value is positive when [position] is safe, negative when it is not,
     * and its magnitude tells how far the position is from the border.
     */
    fun safetyMargin(position: Position): Double

    /**
     * Returns whether [position] belongs to the safe space.
     */
    fun isSafe(position: Position): Boolean = safetyMargin(position) >= 0.0

    /**
     * Predefined [CBF] instances.
     */
    companion object {
        /**
         * A [CBF] modelling an unconstrained space, in which every position is safe:
         * the margin is infinite, hence no border is ever met while looking for safe sectors.
         */
        val unconstrained: CBF = CBF { Double.POSITIVE_INFINITY }
    }
}
