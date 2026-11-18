package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.util.Environments.networkDiameterByHopDistance

/**
 * An Alchemist [Extractor] that computes and exports the network diameter of the simulated environment.
 */
class NetworkDiameter : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-diameter"
    }

    override val columnNames: List<String> = listOf(NAME)

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = mapOf(NAME to environment.networkDiameterByHopDistance())
}
