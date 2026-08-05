package it.unibo.collektive.alchemist.device.sensors

/**
 * Interface representing a random number generator that provides values based on the simulation environment's seed.
 */
interface RandomGenerator {
    /**
     * Generate a random double given the environment seed.
     */
    fun nextRandomDouble(): Double

    /**
     * Generate a random double inside the range of 0.0 and [until] given the environment seed.
     */
    fun nextRandomDouble(until: Double): Double

    /**
     * Generate a random double inside a given [range] given the environment seed.
     */
    fun nextRandomDouble(range: ClosedFloatingPointRange<Double>): Double

    /**
     * Generate a random double following the Gaussian distribution.
     */
    fun nextGaussian(): Double
}
