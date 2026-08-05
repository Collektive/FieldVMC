package it.unibo.common.sdf.impl

import it.unibo.common.pointsDistance
import it.unibo.common.sdf.SDF
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sin

/**
 * Represents a 2D Signed Distance Field (SDF) of an arc.
 *
 * @property center The (X, Y) coordinates of the arc's center.
 * @property radius The radius of the arc.
 * @property startAngle The starting angle of the arc in radians.
 * @property aperture The angular length of the arc in radians.
 * @property thickness The thickness of the arc (default is 0.0).
 */
class Arc(
    private val center: Pair<Double, Double>,
    private val radius: Double,
    private val startAngle: Double,
    private val aperture: Double,
    private val thickness: Double = 0.0,
) : SDF {
    private val endAngle = startAngle + aperture
    private val pS = Pair(center.first + radius * cos(startAngle), center.second + radius * sin(startAngle))
    private val pE = Pair(center.first + radius * cos(endAngle), center.second + radius * sin(endAngle))

    override fun invoke(p: Pair<Double, Double>): Double {
        val angle = atan2(p.second - center.second, p.first - center.first)

        val normalizedAngle = (angle - startAngle).mod(2.0 * PI)

        val distance = if (normalizedAngle <= aperture) {
            abs(radius - pointsDistance(p, center))
        } else {
            min(pointsDistance(p, pS), pointsDistance(p, pE))
        }

        return distance - thickness
    }
}
