package it.unibo.common

/**
 * Represents an angular sector defined by a starting angle and an arc length.
 *
 * @property from The starting angle of the sector.
 * @property arc The span or length of the angular arc.
 */
data class AngularSector(
    val from: Double,
    val arc: Double,
) : Comparable<AngularSector> {
    /**
     * Compares this angle with another based on the arc length first, then the starting angle.
     */
    override fun compareTo(other: AngularSector): Int =
        compareBy(AngularSector::arc)
        .thenBy(AngularSector::from)
        .compare(this, other)
}
