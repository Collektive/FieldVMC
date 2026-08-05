@file:Suppress("DEPRECATION")

package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.swingui.effect.api.Effect
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import kotlin.math.max

/** Shared lifecycle and data access for tree visualization effects. */
abstract class AbstractTreeEffect : Effect {
    private var lastUpdated = Time.NEGATIVE_INFINITY

    /** Maximum success currently present in the environment. */
    protected var maxSuccess = 0.0
        private set

    /** Maximum resource currently present in the environment. */
    protected var maxResource = 0.0
        private set

    final override fun getColorSummary(): Color = Color.BLACK

    final override fun <T : Any?, P : Position2D<P>> apply(
        g: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        updateMaxValues(environment)
        draw(g, node, environment, wormhole)
    }

    /** Draws the effect after the shared resource and success bounds have been updated. */
    protected abstract fun <T : Any?, P : Position2D<P>> draw(
        g: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    )

    private fun <T : Any?, P : Position2D<P>> updateMaxValues(environment: Environment<T, P>) {
        if (environment.simulation.time != lastUpdated) {
            maxSuccess = 0.0
            maxResource = 0.0
            for (node in environment.nodes) {
                maxSuccess = max(maxSuccess, node.getConcentration(treeSuccess).toTreeDouble())
                maxResource = max(maxResource, node.getConcentration(treeResource).toTreeDouble())
            }
            lastUpdated = environment.simulation.time
        }
    }
}

internal val treeParent = SimpleMolecule("parent")
internal val treeSuccess = SimpleMolecule("success")
internal val treeResource = SimpleMolecule("resource")
internal val treeLeader = SimpleMolecule("leader")

internal fun Any?.toTreeInt(): Int? =
    when (this) {
        is Int -> this
        is Number -> toInt()
        is String -> toInt()
        null, Unit -> null
        else -> error("Unexpected integer: $this")
    }

internal fun Any?.toTreeDouble(): Double =
    when (this) {
        is Double -> this
        is Number -> toDouble()
        null, Unit -> 0.0
        else -> error("Unexpected number: $this")
    }

internal operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)

internal operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)
