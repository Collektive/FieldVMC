package it.unibo.common

import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import kotlin.math.PI

/**
 * Selects a [random] element from the iterable,
 * with a probability proportional to the value returned by the [by] function.
 */
fun <T> Iterable<T>.randomElementWeighted(
    random: RandomGenerator,
    by: T.() -> Double,
): T {
    val total = fold(0.0) { acc, element -> acc + by(element) }
    val selector: Double = random.nextRandomDouble(0.0..total)
    var accumulator = 0.0
    for (element in this) {
        accumulator += by(element)
        if (accumulator >= selector) {
            return element
        }
    }
    return last()
}

/**
 * Calculates a new random angle for spawning a child node by finding the gaps between existing [angles]
 * and intersecting them with the provided [safeSectors].
 * A valid sector is chosen with a probability proportional to its arc length (the distance between the
 * previous and next angle).
 * The [maxChildren] parameter is used to calculate the minimum required angle difference.
 */
fun calculateAngle(
    angles: List<Double>,
    random: RandomGenerator,
    maxChildren: Int,
    safeSectors: List<AngularSector>,
): Double {
    fun relativeAngleTowards(center: Double) = PI * random.nextGaussian() / maxChildren + center

    if (safeSectors.isEmpty()) return Double.NaN

    val minArc = 2 * PI / maxChildren

    val differences =
        when {
        angles.isEmpty() -> listOf(AngularSector(0.0, 2 * PI))
        angles.size == 1 -> listOf(AngularSector(angles.first(), 2 * PI))
        else -> {
            val sortedAngles = angles.sorted()
            val fullCircle = sortedAngles + (sortedAngles.first() + 2 * PI)
            fullCircle
                .zipWithNext { a, b -> AngularSector(a, b - a) }
                .filter { it.arc >= minArc }
        }
    }

    val linearDiffs = differences.flatMap { splitAtZero(it) }
    val linearSafe = safeSectors.flatMap { splitAtZero(it) }

    val rawIntersections = mutableListOf<AngularSector>()
    for (diff in linearDiffs) {
        for (safe in linearSafe) {
            val start = maxOf(diff.from, safe.from)
            val end = minOf(diff.from + diff.arc, safe.from + safe.arc)

            if (start < end) { // Se c'è sovrapposizione
                rawIntersections.add(AngularSector(start, end - start))
            }
        }
    }

    val intersection = rawIntersections.sortedBy { it.from }

    val validIntersections = if (angles.isNotEmpty() && angles.minOf { it } != 0.0) {
        wrapSectors(intersection)
    } else {
        intersection
    }

    return when {
        validIntersections.isEmpty() -> Double.NaN
        else -> {
            val selected = validIntersections.randomElementWeighted(random) { arc }
            relativeAngleTowards(selected.arc / 2 + selected.from)
                .coerceIn(selected.from, selected.from + selected.arc)
        }
    }
}

/**
 * Given [sector] it normalizes it in the range [0. 2PI)
 * If the arc passes the 0, it is split in two normalized arcs
 */
fun splitAtZero(sector: AngularSector): List<AngularSector> {
    val start = ((sector.from % (2 * PI)) + 2 * PI) % (2 * PI)
    val end = start + sector.arc

    return if (end > 2 * PI) {
        listOf(
            AngularSector(start, 2 * PI - start),
            AngularSector(0.0, end - 2 * PI),
        )
    } else {
        listOf(AngularSector(start, sector.arc))
    }
}
