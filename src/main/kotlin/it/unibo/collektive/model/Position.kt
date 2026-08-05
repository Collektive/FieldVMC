package it.unibo.collektive.model

/**
 * A two-dimensional position in the simulation coordinate system.
 *
 * @property x The horizontal coordinate.
 * @property y The vertical coordinate.
 */
data class Position(val x: Double, val y: Double)

/** Adds the components of [position] to this position. */
operator fun Position.plus(position: Position): Position = Position(x + position.x, y + position.y)

/** Subtracts the components of [position] from this position. */
operator fun Position.minus(position: Position): Position = Position(x - position.x, y - position.y)
