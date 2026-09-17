package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.swingui.effect.api.LayerToFunctionMapper
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * A [LayerToFunctionMapper] sampling every [Layer] through its own `getValue`,
 * whatever its concrete implementation is.
 *
 * [BidimensionalGaussianLayersMapper] only maps the layers which are a `BidimensionalGaussianLayer`,
 * silently dropping all the others: the layers scheduling their sources over time
 * (`ScheduledGaussianLayer` and `TimedLayer`) would never be drawn.
 * This mapper draws them, asking each layer for its current value, and treats as zero the values
 * which are not a number.
 */
class AnyLayerToFunctionMapper : LayerToFunctionMapper {
    override fun <T, P : Position2D<P>> map(layers: Collection<Layer<T, P>>): Collection<Function<in P, out Number>> =
        layers.map { layer ->
            Function<P, Number> { position -> layer.getValue(position) as? Number ?: NOT_A_NUMBER }
        }

    override fun <T, P : Position2D<P>> map(layers: Stream<Layer<T, P>>): Stream<Function<in P, out Number>> =
        map(layers.collect(Collectors.toList())).stream()

    /**
     * Constants for the [AnyLayerToFunctionMapper].
     */
    companion object {
        /** The value drawn where a layer does not provide a number. */
        private const val NOT_A_NUMBER = 0.0

        private const val serialVersionUID: Long = 1L
    }
}
