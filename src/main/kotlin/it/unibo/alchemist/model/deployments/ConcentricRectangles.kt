package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.absoluteValue

/**
 * An Alchemist deployment strategy that places nodes randomly within the space
 * between an outer rectangle and an inner rectangle.
 *
 * This effectively creates a "hollow" rectangular deployment area, where the
 * nodes are distributed proportionally based on the area of the four sub-rectangles
 * that make up the bounding frame.
 *
 * @param P The position type.
 * @param environment The environment where the nodes will be deployed.
 * @param randomGenerator The random engine used for positioning.
 * @param nodes The total number of nodes to deploy.
 * @param outerStartX The X coordinate of the outer rectangle's top-left corner.
 * @param outerStartY The Y coordinate of the outer rectangle's top-left corner.
 * @param outerWidth The total width of the outer rectangle.
 * @param outerHeight The total height of the outer rectangle.
 * @param innerStartX The X coordinate of the inner rectangle's top-left corner.
 * @param innerStartY The Y coordinate of the inner rectangle's top-left corner.
 * @param innerWidth The width of the inner (empty) rectangle.
 * @param innerHeight The height of the inner (empty) rectangle.
 */
class ConcentricRectangles<P: Position<P>>(
    environment: Environment<*, P>,
    randomGenerator: RandomGenerator,
    val nodes: Int,
    outerStartX: Double,
    outerStartY: Double,
    outerWidth: Double,
    outerHeight: Double,
    innerStartX: Double,
    innerStartY: Double,
    innerWidth: Double,
    innerHeight: Double,
) : AbstractRandomDeployment<P>(environment, randomGenerator, nodes) {

    /**
     * Represents a 2D rectangular area used to partition the deployment space.
     *
     * @property startX The X coordinate of the top-left corner.
     * @property startY The Y coordinate of the top-left corner.
     * @property width The width of the rectangle.
     * @property height The height of the rectangle.
     */
    data class Rectangle(val startX: Double, val startY: Double, val width: Double, val height: Double){

        /**
         * The total area of the rectangle.
         */
        val area = width * height
        override fun toString(): String =
            "Rectangle(start($startX, $startY), finish(${startX + width}, ${startY + height})), " +
                "width: $width, height: $height, area: $area"
    }

    /**
     * Companion object holding the shared deployment rectangles.
     */
    companion object{
        /**
         * List of rectangles representing the available deployment areas.
         */
        lateinit var rectangles : List<Rectangle>
    }

    init {
        rectangles = listOf<Rectangle>(
            Rectangle(outerStartX, outerStartY, (innerStartX - outerStartX).absoluteValue, outerHeight),
            Rectangle(innerStartX, outerStartY, innerWidth, (innerStartY - outerStartY).absoluteValue),
            Rectangle(
                innerStartX,
                innerStartY + innerHeight,
                innerWidth,
                ((outerStartY + outerHeight) - (innerStartY + innerHeight)).absoluteValue),
            Rectangle(
                innerStartX + innerWidth,
                outerStartY,
                ((outerStartX + outerWidth) - (innerStartX + innerWidth)).absoluteValue,
                outerHeight),
        )
    }

    override fun indexToPosition(i: Int): P {
        val randomArea = randomGenerator.nextDouble(0.0, rectangles.sumOf { it.area })
        var currentArea = 0.0
        return rectangles.first {
            currentArea += it.area
            randomArea <= currentArea
        }.let { rectangle ->
            environment.makePosition(
                randomGenerator.nextDouble(rectangle.startX, rectangle.startX + rectangle.width),
                randomGenerator.nextDouble(rectangle.startY, rectangle.startY + rectangle.height)
            )
        }
    }
}
