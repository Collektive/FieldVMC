package it.unibo.common

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.util.Environments.networkDiameterByHopDistance
import it.unibo.common.NetworkMetrics.networkDensity
import it.unibo.common.NetworkMetrics.networkHub
import it.unibo.common.NetworkMetrics.nodesDegree

/**
 * A functional object that extracts and computes termination and topological metrics
 * from a given simulation environment.
 */
class TerminationMetrics : (Environment<*, *>) -> Map<String, Double> {
    /**
     * Evaluates the environment and extracts a map of network metrics.
     *
     * @param env The simulation environment to extract metrics from.
     * @return A map containing computed metrics such as node count, hub coordinates,
     * density, diameter, and average node degree.
     */
    override fun invoke(env: Environment<*, *>): Map<String, Double> =
        env.networkHub().let { (xCoord, yCoord) ->
            mapOf(
                "nodes" to env.nodeCount.toDouble(),
                "network-hub-xCoord" to xCoord,
                "network-hub-yCoord" to yCoord,
                "network-density" to env.networkDensity(),
                "network-diameter" to env.networkDiameterByHopDistance(),
                "nodes-degree[mean]" to env.nodesDegree().average(),
            )
        }
}
