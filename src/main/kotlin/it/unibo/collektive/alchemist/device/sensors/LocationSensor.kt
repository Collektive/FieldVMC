package it.unibo.collektive.alchemist.device.sensors

/**
 * Interface representing a sensor capable of retrieving the location of the node and its neighbors.
 */
interface LocationSensor {
    /**
     * Returns the coordinates of the node's position inside the environment.
     */
    fun coordinates(): Pair<Double, Double>

    /**
     * Returns the coordinates of the neighborhood.
     */
    fun surroundings(): List<Pair<Double, Double>>
}
