package it.unibo.common

import kotlin.math.PI

/**
 * Identifies and returns a list of angular sectors that lie entirely within a safe region.
 *
 * The safe region is determined along a circumference of radius [r] using the provided [validator]
 * function, which computes the distance from a specific point (given its angle) to the boundary.
 */
fun findSafeSectors(
    r: Double,
    validator: (Double) -> Double,
): List<AngularSector> {
    val zeros = findZeros(r, validator).sorted()

    if (zeros.isEmpty()) {
        val fullCircle = AngularSector(0.0, 2 * PI)
        return if (isSectorSafe(fullCircle, validator)) listOf(fullCircle) else emptyList()
    }

    val wrappedZeros = zeros + (zeros.first() + 2 * PI)
    val safeSectors =
        wrappedZeros
        .zipWithNext { a, b -> AngularSector(a, b - a) }
        .filter { isSectorSafe(it, validator) }

    return mergeSectors(safeSectors)
}

/**
 * Determines whether a given [sector] describes a valid (safe) region.
 *
 * The function evaluates the arc's safety by sampling points using the provided [validator].
 * Due to the construction properties, the entire arc shares the same validity as any of its
 * sampled points that do not evaluate to zero.
 *
 * If the [validator] evaluates to `0.0` (indicating the point lies exactly on a boundary),
 * the algorithm iteratively samples additional points at increasing depths up to a maximum limit.
 * If all tested points evaluate to `0.0`, the arc is assumed to perfectly overlap the boundary
 * and is considered safe.
 */
fun isSectorSafe(
    sector: AngularSector,
    validator: (Double) -> Double,
): Boolean {
    val maxDepth = 10
    var divisor = 2.0
    var isSafe = true
    var found = false
    var currentDepth = 0

    while (currentDepth < maxDepth && !found) {
        val step = sector.arc / divisor

        val dLeft = validator(sector.from + step)
        if (dLeft != 0.0) {
            isSafe = dLeft > 0.0
            found = true
        } else {
            val dRight = validator(sector.from + sector.arc - step)
            if (dRight != 0.0) {
                isSafe = dRight > 0.0
                found = true
            }
        }

        divisor *= 2.0
        currentDepth++
    }

    return isSafe
}
