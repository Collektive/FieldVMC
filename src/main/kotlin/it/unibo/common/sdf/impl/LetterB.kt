package it.unibo.common.sdf.impl

import it.unibo.common.sdf.SDF
import kotlin.math.PI

/**
 * Represents a 2D Signed Distance Field (SDF) of the letter B.
 *
 * @param start The (X, Y) coordinates of the starting point of the vertical stem.
 * @param height The total height of the letter.
 * @property thickness The thickness of the letter's strokes (default is 0.0).
 */
class LetterB(
    start: Pair<Double, Double>,
    height: Double,
    private val thickness: Double = 0.0,
) : SDF {
    private val vertical = Segment(start, Pair(start.first, start.second + height))
    private val lowArc =
        Arc(
        Pair(start.first + height / QUARTER_DIVISOR, start.second + height / QUARTER_DIVISOR),
        height / QUARTER_DIVISOR,
        -PI / HALF_DIVISOR,
        PI,
    )
    private val highArc =
        Arc(
        Pair(start.first + height / EIGHTH_DIVISOR, start.second + height * THREE_QUARTERS),
        height / QUARTER_DIVISOR,
        -PI / HALF_DIVISOR,
        PI,
    )
    private val lowSeg = Segment(start, Pair(start.first + height / QUARTER_DIVISOR, start.second))
    private val midSeg =
        Segment(
        Pair(start.first, start.second + height / HALF_DIVISOR),
        Pair(start.first + height / QUARTER_DIVISOR, start.second + height / HALF_DIVISOR)
    )
    private val highSeg =
        Segment(
        Pair(start.first, start.second + height),
        Pair(start.first + height / EIGHTH_DIVISOR, start.second + height)
    )

    override fun invoke(p: Pair<Double, Double>): Double =
        minOf(vertical(p), highArc(p), lowArc(p), lowSeg(p), midSeg(p), highSeg(p)) - thickness

    /**
     * Constants used for proportional geometry calculations.
     */
    companion object {
        private const val HALF_DIVISOR = 2.0
        private const val QUARTER_DIVISOR = 4.0
        private const val EIGHTH_DIVISOR = 8.0
        private const val THREE_QUARTERS = 0.75
    }
}
