package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position

/**
 * The simulated time, or `0.0` if the simulation has not been attached to the environment yet
 * (layers may be queried while the simulation is still being loaded).
 */
internal fun Environment<*, *>.simulationTime(): Double = runCatching { simulation.time.toDouble() }.getOrDefault(0.0)

/**
 * A [Layer] with a lifetime: it delegates to [layer] while the simulated time is in `[`[from], [until]`)`,
 * and returns [whenInactive] outside such an interval.
 *
 * It can wrap any other layer, e.g. to make a gaussian source appear at time 100 and vanish at time 500:
 * ```yaml
 * layers:
 *   - molecule: localResource
 *     type: TimedLayer
 *     parameters:
 *       - { type: BidimensionalGaussianLayer, parameters: [0, 0, 1000, 5] }
 *       - 100
 *       - 500
 * ```
 *
 * @property environment the simulated environment, injected by the loader, used to read the current time.
 * @property layer the layer providing the values while this one is active.
 * @property from the time at which the layer starts providing its values (`0.0`, i.e. immediately, by default).
 * @property until the time at which the layer stops providing its values (never, by default).
 * @property whenInactive the value provided outside the lifetime of the layer (`0.0` by default).
 */
class TimedLayer<P : Position<P>>
@JvmOverloads
constructor(
    private val environment: Environment<*, P>,
    private val layer: Layer<Double, P>,
    private val from: Double = 0.0,
    private val until: Double = Double.POSITIVE_INFINITY,
    private val whenInactive: Double = 0.0,
) : Layer<Double, P> {
    init {
        require(from < until) { "A layer cannot disappear at $until, before appearing at $from." }
    }

    override fun getValue(p: P): Double = when (environment.simulationTime()) {
        in from..<until -> layer.getValue(p)
        else -> whenInactive
    }

    override fun toString(): String = "${this::class.simpleName}($layer, from=$from, until=$until)"

    /**
     * Companion object containing the serialization version UID.
     */
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
