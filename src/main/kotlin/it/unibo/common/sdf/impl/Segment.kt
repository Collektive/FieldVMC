package it.unibo.common.sdf.impl

import it.unibo.common.pointsDistance
import it.unibo.common.sdf.SDF

/**
 * Represents a 2D Signed Distance Field (SDF) of a line segment.
 *
 * @property a The (X, Y) coordinates of the starting point of the segment.
 * @property b The (X, Y) coordinates of the ending point of the segment.
 * @property thickness The thickness of the segment (default is 0.0).
 */
class Segment(
    private val a: Pair<Double, Double>,
    private val b: Pair<Double, Double>,
    private val thickness: Double = 0.0,
) : SDF {
    override fun invoke(p: Pair<Double, Double>): Double {
        val abX = b.first - a.first
        val abY = b.second - a.second
        val apX = p.first - a.first
        val apY = p.second - a.second

        val abLenSq = abX * abX + abY * abY

        if (abLenSq == 0.0) return pointsDistance(p, a)

        val t = (apX * abX + apY * abY) / abLenSq

        val tClamped = t.coerceIn(0.0, 1.0)

        val closestX = a.first + tClamped * abX
        val closestY = a.second + tClamped * abY

        return pointsDistance(p, Pair(closestX, closestY)) - thickness
    }
}
