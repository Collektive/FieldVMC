package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.common.NetworkMetrics.networkDensity

/**
 * An Alchemist [Extractor] that computes and exports the network density of the simulated environment.
 */
class NetworkDensity : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-density"
    }

    override val columnNames: List<String> = listOf(NAME)

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = mapOf(NAME to environment.networkDensity())
}
