package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time

/**
 * An Alchemist [Extractor] that computes the geometric center (hub) of the network.
 */
class NetworkHub : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-hub"
        private const val POSITIVE_OFFSET: Double = 10.0
    }

    override val columnNames: List<String>
        get() = listOf<String>("$NAME-xCoord", "$NAME-yCoord")

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> {
        val sum =
            environment.nodes.current.fold(0.0 to 0.0) { acc, next ->
                // Add 10 to avoid negative positions
                val nodePos = environment.currentPositionOf(next).coordinates.map { it + POSITIVE_OFFSET }
                acc.first + nodePos[0] to acc.second + nodePos[1]
            }
        val center = sum.first / environment.nodeCount.current to sum.second / environment.nodeCount.current
        return center.toList().mapIndexed { index, value -> "$NAME@$index" to value }.toMap()
    }
}
