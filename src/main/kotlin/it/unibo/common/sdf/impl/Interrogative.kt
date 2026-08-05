package it.unibo.common.sdf.impl

import it.unibo.common.sdf.SDF
import kotlin.math.PI
import kotlin.math.min

/**
 * Represents a 2D Signed Distance Field (SDF) of a shape composed of an arc and a segment
 * (resembling a question mark or a hook).
 *
 * @param center The (X, Y) coordinates of the arc's center.
 * @param radius The radius of the arc, which also dictates the length and position of the segment.
 * @property thickness The thickness of the shape (default is 0.0).
 */
class Interrogative(
    center: Pair<Double, Double>,
    radius: Double,
    private val thickness: Double = 0.0,
) : SDF {
    private val arc =
        Arc(
        center,
        radius,
        -PI / 2.0,
        3.0 / 2.0 * PI
    )
    private val segment = Segment(
        Pair(center.first, center.second - radius),
        Pair(center.first, center.second - 2.0 * radius)
    )

    override fun invoke(p: Pair<Double, Double>): Double =
        min(arc(p), segment(p)) - thickness
}
