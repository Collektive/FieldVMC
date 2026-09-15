package it.unibo.collektive.utils

/**
 * A point-like contract in a bidimensional space, exposing its coordinates as an ([x], [y]) pair.
 */
interface Vector2D {
    /** The horizontal coordinate. */
    val x: Double

    /** The vertical coordinate. */
    val y: Double

    /** The number of dimensions of the space this vector belongs to. */
    val dimension: Int
        get() = 2
}

/**
 * The velocity of a device, expressed through its components along the [x] and [y] axes.
 */
data class SpeedControl2D(override val x: Double, override val y: Double) : Vector2D
