@file:Suppress("DEPRECATION")

package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.swingui.effect.api.Effect
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.nextUp

/**
 * An Alchemist [Effect] that draws nodes of a tree structure in the UI.
 *
 * It visualizes individual nodes, scaling their size and determining their color
 * based on their local success and resource concentrations. Leaders are highlighted
 * with special concentric arcs.
 */
class DrawTreeNodes : Effect {
    override fun getColorSummary(): Color = Color.BLACK

    /**
     * The timestamp of the last update, used to prevent redundant iteration over nodes.
     */
    @Transient
    var lastUpdated = Time.NEGATIVE_INFINITY

    /**
     * The maximum local success value found among all nodes.
     */
    @Transient
    var maxSuccess = 0.0

    /**
     * The maximum resource value found among all nodes.
     */
    @Transient
    var maxResource = 0.0

    override fun <T : Any?, P : Position2D<P>> apply(
        g: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        updateMaxValues(environment)

        runCatching {
            environment.getPosition(node)
        }.onSuccess { nodePosition ->
            val viewPoint = wormhole.getViewPoint(nodePosition)
            val localResource = node.getConcentration(resource).toDouble()
            val localSuccess = node.getConcentration(localSuccess).toDouble()
            val isLeader = node.getConcentration(leader) == true

            val size = calculateNodeSize(localResource, localSuccess)
            val sizeInScreenCoordinates = calculateScreenSize(size, nodePosition, environment, wormhole, viewPoint)

            val boundingBoxSize =
                when {
                    isLeader -> sizeInScreenCoordinates
                    else -> sizeInScreenCoordinates / BOUNDING_BOX_DIVISOR
                }

            val boundingBox =
                listOf(
                    viewPoint + boundingBoxSize,
                    viewPoint + boundingBoxSize.mirrorX(),
                    viewPoint + boundingBoxSize.mirrorY(),
                    viewPoint - boundingBoxSize,
                )

            if (isLeader) {
                drawLeaderArcs(g, viewPoint, sizeInScreenCoordinates)
            }

            if (boundingBox.any { wormhole.isInsideView(it) }) {
                drawNodeBody(g, viewPoint, sizeInScreenCoordinates, localResource, localSuccess, size.toFloat())
            }
        }
    }

    private fun <T : Any?, P : Position2D<P>> updateMaxValues(environment: Environment<T, P>) {
        if (environment.simulation.time != lastUpdated) {
            maxSuccess = 0.0
            maxResource = 0.0
            for (currentNode in environment.nodes) {
                maxSuccess = max(maxSuccess, currentNode.getConcentration(localSuccess).toDouble())
                maxResource = max(maxResource, currentNode.getConcentration(resource).toDouble())
            }
            lastUpdated = environment.simulation.time
        }
    }

    private fun calculateNodeSize(
        localResource: Double,
        localSuccess: Double,
    ): Double =
        when {
            localResource > 0 && localSuccess > 0 -> localResource / maxResource + localSuccess / maxSuccess
            localResource > 0 -> localResource / maxResource
            localSuccess > 0 -> localSuccess / maxSuccess
            else -> 0.0.nextUp()
        }

    private fun <P : Position2D<P>> calculateScreenSize(
        size: Double,
        nodePosition: P,
        environment: Environment<*, P>,
        wormhole: Wormhole2D<P>,
        viewPoint: Point,
    ): Point {
        val sizeAsPosition: P = environment.makePosition(size, size)
        val sizeFromLocation = sizeAsPosition + nodePosition.coordinates
        return (wormhole.getViewPoint(sizeFromLocation) - viewPoint)
            .let { Point(abs(it.x), abs(it.y)) }
            .takeIf { it.x > minNodeSize && it.y > minNodeSize }
            ?: Point(minNodeSize, minNodeSize)
    }

    private fun drawLeaderArcs(
        g: Graphics2D,
        viewPoint: Point,
        sizeInScreenCoordinates: Point,
    ) {
        g.stroke =
            BasicStroke(
                LEADER_STROKE_SCALE * sizeInScreenCoordinates.x.toFloat(),
                BasicStroke.CAP_ROUND,
                BasicStroke.CAP_BUTT
            )

        listOf(
            ARC_NUM_1 to ARC_DEN_1,
            ARC_NUM_2 to ARC_DEN_2,
        ).forEach { (numerator, denominator) ->
            g.color = LEADER_ARC_COLOR
            val arcX = viewPoint.x - sizeInScreenCoordinates.x * numerator / denominator
            val arcY = viewPoint.y - sizeInScreenCoordinates.y * numerator / denominator
            val arcWidth = sizeInScreenCoordinates.x * ARC_DIAMETER_MULTIPLIER * numerator / denominator
            val arcHeight = sizeInScreenCoordinates.y * ARC_DIAMETER_MULTIPLIER * numerator / denominator

            g.drawArc(
                arcX,
                arcY,
                arcWidth,
                arcHeight,
                0,
                FULL_CIRCLE_DEGREES,
            )
        }
    }

    private fun drawNodeBody(
        g: Graphics2D,
        viewPoint: Point,
        sizeInScreenCoordinates: Point,
        localResource: Double,
        localSuccess: Double,
        size: Float,
    ) {
        val hue = (localResource / maxResource).toFloat() * HSB_HUE_MULTIPLIER
        val brightness = (localSuccess / maxSuccess).toFloat() / HSB_BRIGHTNESS_DIVISOR + HSB_BRIGHTNESS_OFFSET
        g.color = Color.getHSBColor(hue, HSB_SATURATION, brightness)

        val x = viewPoint.x - sizeInScreenCoordinates.x / HALF
        val y = viewPoint.y - sizeInScreenCoordinates.y / HALF

        g.fillOval(x, y, sizeInScreenCoordinates.x, sizeInScreenCoordinates.y)

        g.color = Color.BLACK
        g.stroke = BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.CAP_BUTT)
        g.drawArc(
            x,
            y,
            sizeInScreenCoordinates.x,
            sizeInScreenCoordinates.y,
            0,
            FULL_CIRCLE_DEGREES,
        )
    }

    /**
     * Utility functions, molecules, and constants for rendering.
     */
    companion object {
        /**
         * Molecule used to identify the parent of a node.
         */
        val myParent = SimpleMolecule("parent")

        /**
         * Molecule representing the local success of a node.
         */
        val localSuccess = SimpleMolecule("success")

        /**
         * Molecule representing the local resource of a node.
         */
        val resource = SimpleMolecule("resource")

        /**
         * Molecule indicating if a node is a leader.
         */
        val leader = SimpleMolecule("leader")

        /**
         * The minimum visual size of a node on the screen.
         */
        const val minNodeSize = 10

        private const val LEADER_STROKE_SCALE = 0.1f
        private const val LEADER_ARC_COLOR_HEX = 0x333333
        private val LEADER_ARC_COLOR = Color(LEADER_ARC_COLOR_HEX)

        private const val ARC_NUM_1 = 1
        private const val ARC_DEN_1 = 2
        private const val ARC_NUM_2 = 6
        private const val ARC_DEN_2 = 9

        private const val ARC_DIAMETER_MULTIPLIER = 2
        private const val FULL_CIRCLE_DEGREES = 360
        private const val HSB_HUE_MULTIPLIER = 0.8f
        private const val HSB_SATURATION = 1.0f
        private const val HSB_BRIGHTNESS_DIVISOR = 2f
        private const val HSB_BRIGHTNESS_OFFSET = 0.5f
        private const val BOUNDING_BOX_DIVISOR = 2
        private const val HALF = 2

        private fun Any?.toInt(): Int? =
            when (this) {
                is Int -> this
                is Number -> this.toInt()
                is String -> this.toInt()
                null -> null
                Unit -> null
                else -> error("Unexpected integer: $this")
            }

        private fun Any?.toDouble(): Double =
            when (this) {
                is Double -> this
                is Number -> this.toDouble()
                null -> 0.0
                Unit -> 0.0
                else -> error("Unexpected integer: $this")
            }

        private operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)

        private operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)

        private operator fun Point.times(factor: Int): Point = Point((x * factor), (y * factor))

        private operator fun Point.div(factor: Int): Point = Point((x / factor), (y / factor))

        private fun Point.mirrorX(): Point = Point(-x, y)

        private fun Point.mirrorY(): Point = Point(x, -y)
    }
}
