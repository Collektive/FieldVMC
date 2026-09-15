package it.unibo.collektive.utils

/**
 * 2D [dimension] point-like contract exposing coordinates and
 * a self-referencing [position] for DSLs that expect a `([x], [y])` pair.
 */
interface Vector2D {
    val x: Double
    val y: Double

    val dimension: Int
        get() = 2
}

data class SpeedControl2D(override val x: Double, override val y: Double): Vector2D
