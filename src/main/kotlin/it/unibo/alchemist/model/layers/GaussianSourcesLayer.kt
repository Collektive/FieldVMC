package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D

/**
 * A layer hosting several gaussian sources at once, whose value in a point is the sum of the values
 * every source provides there.
 *
 * All the sources share the same [norm] and [sigma], and sit in the given [positions], each of them
 * an `[x, y]` pair. It is the way to put more sources than layers under the same molecule:
 * ```yaml
 * layers:
 *   - molecule: successSource
 *     type: GaussianSourcesLayer
 *     parameters:
 *       - 100000   # norm
 *       - 5        # sigma
 *       - [[0, 30], [-20, 15], [20, 15]]
 * ```
 *
 * @param norm the peak value of each source, as in [BidimensionalGaussianLayer].
 * @param sigma the standard deviation of each source, as in [BidimensionalGaussianLayer].
 * @param positions where the sources sit, as `[x, y]` pairs.
 */
class GaussianSourcesLayer<P : Position2D<P>>(norm: Double, sigma: Double, positions: List<List<Number>>) :
    Layer<Double, P> {
    private val sources: List<BidimensionalGaussianLayer<P>> = positions.map { position ->
        require(position.size == COORDINATES) {
            "The position of a source must be an [x, y] pair, but $position was found."
        }
        BidimensionalGaussianLayer(
            centerX = position.first().toDouble(),
            centerY = position.last().toDouble(),
            norm = norm,
            sigmaX = sigma,
        )
    }

    init {
        require(sources.isNotEmpty()) { "At least one source is required." }
    }

    override fun getValue(p: P): Double = sources.sumOf { it.getValue(p) }

    override fun toString(): String = "${this::class.simpleName}($sources)"

    /**
     * Constants for the [GaussianSourcesLayer].
     */
    companion object {
        /** The number of coordinates describing the position of a source. */
        private const val COORDINATES = 2

        private const val serialVersionUID: Long = 1L
    }
}
