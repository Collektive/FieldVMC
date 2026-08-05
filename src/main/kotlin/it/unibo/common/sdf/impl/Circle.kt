package it.unibo.common.sdf.impl

import it.unibo.common.pointsDistance
import it.unibo.common.sdf.SDF
import kotlin.math.abs

/**
 * Represents a 2D Signed Distance Field (SDF) of a circle or a ring.
 *
 * @property center The (X, Y) coordinates of the circle's center.
 * @property radius The radius of the circle.
 * @property isRing True if the shape is a hollow ring instead of a solid circle (default is false).
 * @property thickness The thickness of the ring if [isRing] is true (default is 0.0).
 */
class Circle(
    private val center: Pair<Double, Double>,
    private val radius: Double,
    private val isRing: Boolean = false,
    private val thickness: Double = 0.0,
) : SDF {
    override fun invoke(p: Pair<Double, Double>): Double {
        val circleDist = radius - pointsDistance(p, center)

        return if (isRing) abs(circleDist) - thickness else circleDist
    }
}
