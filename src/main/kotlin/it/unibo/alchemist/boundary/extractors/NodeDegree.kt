package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.ExportFilter
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule

/**
 * An Alchemist extractor that computes the degree (number of neighbors) of each node in the environment.
 *
 * It extends [AbstractAggregatingDoubleExtractor] to automatically aggregate the results (e.g., mean, max)
 * across all nodes. It can optionally filter the neighborhood to count only nodes that share a
 * hierarchical relationship (parent-child).
 */
class NodeDegree
@JvmOverloads
constructor(
    filter: ExportFilter,
    aggregators: List<String>,
    private val checkChildren: Boolean = false,
    precision: Int = 2,
) : AbstractAggregatingDoubleExtractor(filter, aggregators, precision) {
    private companion object {
        private const val NAME = "nodes-degree"
    }

    override val columnName: String = NAME

    override fun <T> getData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long,
    ): Map<Node<T>, Double> = environment.nodes.current.associateWith { node ->
        val neighbors = environment.neighborhoodOf(node).current.neighbors
        if (checkChildren) {
            neighbors.count { neighbor ->
                neighbor[SimpleMolecule("parent")].getOrNull() == node.id ||
                    node[SimpleMolecule("parent")].getOrNull() == neighbor.id
            }
        } else {
            neighbors.size
        }.toDouble()
    }
}
