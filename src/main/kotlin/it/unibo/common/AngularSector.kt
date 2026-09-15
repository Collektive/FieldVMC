package it.unibo.common

import kotlin.math.PI

/**
 * Represents an angular sector defined by a starting angle and an arc length.
 *
 * @property from The starting angle of the sector.
 * @property arc The span or length of the angular arc.
 */
data class AngularSector(val from: Double, val arc: Double) : Comparable<AngularSector> {
    /**
     * Compares this sector with another one based on the starting angle first, then on the arc length,
     * so that a sorted list of sectors follows the circumference counterclockwise.
     */
    override fun compareTo(other: AngularSector): Int = compareBy(AngularSector::from)
        .thenBy(AngularSector::arc)
        .compare(this, other)

    /**
     * Commonly used angular sectors.
     */
    companion object {
        /**
         * The sector spanning the whole circumference, i.e. the absence of any angular constraint.
         */
        val fullCircle = AngularSector(0.0, 2 * PI)
    }
}
