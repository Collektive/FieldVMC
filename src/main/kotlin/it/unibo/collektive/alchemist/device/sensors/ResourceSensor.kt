package it.unibo.collektive.alchemist.device.sensors

/**
 * Interface representing a sensor capable of tracking and managing resource values for a node.
 */
interface ResourceSensor {

    /**
     * The lower bound threshold for the resource.
     */
    val resourceLowerBound: Double

    /**
     * The maximum capacity or upper bound for the resource.
     */
    val maxResource: Double

    /**
     * Get the value of the resource layer at a given position.
     */
    fun getResource(): Double

    /**
     * Set a new [resource] value for this node.
     */
    fun setCurrentOverallResource(resource: Double)
}
