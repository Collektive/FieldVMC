package it.unibo.common

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sign

/**
 * The minimum distance threshold used to calculate the next angular step.
 * If the computed distance to the boundary falls below this value, [MIN] is used
 * instead to prevent the algorithm from taking excessively small steps when
 * approaching an intersection.
 */
const val MIN = 1e-7

/**
 * The tolerance threshold for the bisection algorithm.
 * The search stops when the search interval is smaller than this value.
 */
const val TOLERANCE = 1e-10

/**
 * A scaling factor applied to the distance from the boundary.
 * Used to compensate for minor calculation inaccuracies or Signed Distance Field (SDF) approximations.
 */
const val IMPRECISION = 0.95

/**
 * Finds the angles at which a circumference of radius [r] intersects a boundary.
 *
 * Iterates along the circumference and uses the [validator] function to detect sign changes,
 * which indicate an intersection between the circumference and the border.
 *
 * Note: Tangency points are not detected unless the sample falls exactly on them,
 * because there is no sign change. Furthermore, if two zero-crossings are closer
 * to each other than [MIN], they might be skipped and not detected.
*/
fun findZeros(
    r: Double,
    validator: (Double) -> Double,
): List<Double> {
    val zeros = mutableListOf<Double>()
    var angle = 0.0
    var prev = 0.0
    var prevd = 0.0

    while (angle < 2 * PI) {
        var d = validator(angle) * IMPRECISION

        if (prevd != 0.0 && d != 0.0 && sign(d) != sign(prevd)) {
            zeros.add(bisection(angle, prev, validator))
        }

        prev = angle
        prevd = d
        d = abs(d)

        if (d > 2 * r) {
            return zeros
        } else {
            if (d == 0.0) {
                zeros.add(angle)
            }
            val delta = if (d < MIN) 2 * asin(MIN / (2 * r)) else 2 * asin((d) / (2 * r))
            angle += delta
        }
    }

    val dFirst = validator(0.0)
    if (prevd != 0.0 && dFirst != 0.0 && sign(dFirst) != sign(prevd)) {
        zeros.add(bisection(2 * PI, prev, validator))
    }

    return zeros
}

/**
 * Finds the root of the [validator] function within the interval defined by [first] and [second]
 * using the bisection method.
 *
 * @param first The first bound of the interval.
 * @param second The second bound of the interval.
 * @param validator The function for which the root is being found.
 * @return The angle (in radians) that represents the root.
 * @throws IllegalArgumentException If [first] and [second] do not bracket a root (i.e., they have the same sign).
 */
fun bisection(
    first: Double,
    second: Double,
    validator: (Double) -> Double,
): Double {
    val vFirst = validator(first)
    val vSecond = validator(second)

    require(sign(vFirst) * sign(vSecond) == -1.0) {
        "Bisection invalid: provided bounds must have opposite signs."
    }

    var pos = if (vFirst > 0.0) first else second
    var neg = if (vFirst < 0.0) first else second
    var candidate: Double

    do {
        candidate = (pos + neg) / 2.0
        val d = validator(candidate)

        if (d < 0.0) {
            neg = candidate
        } else if (d > 0.0) {
            pos = candidate
        } else {
            return candidate
        }
    } while (abs(pos - neg) > TOLERANCE)

    return candidate
}
