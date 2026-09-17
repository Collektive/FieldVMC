package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D

/**
 * A bidimensional gaussian source whose lifetime and position are scheduled through a list of [keyframes].
 *
 * Every keyframe is a `[time, x, y]` triple, meaning *from [time] on, the source sits in `(x, y)`*,
 * and holds until the next keyframe. A keyframe made of the `[time]` alone switches the source off,
 * which is also the state before the first keyframe. The source always has the same [norm] and [sigma],
 * so that only its presence and its position change over time.
 *
 * A source sitting in `(0, 0)`, moving to `(20, 0)` at time 400 and disappearing at time 800:
 * ```yaml
 * layers:
 *   - molecule: localResource
 *     type: ScheduledGaussianLayer
 *     parameters:
 *       - 1000    # norm
 *       - 5       # sigma
 *       - [[0, 0, 0], [400, 20, 0], [800]]
 * ```
 *
 * @property environment the simulated environment, injected by the loader, used to read the current time.
 * @param norm the peak value of the source, as in [BidimensionalGaussianLayer].
 * @param sigma the standard deviation of the source, as in [BidimensionalGaussianLayer].
 * @param keyframes when, where, and whether the source exists, in the form described above.
 */
class ScheduledGaussianLayer<P : Position2D<P>>(
    private val environment: Environment<*, P>,
    norm: Double,
    sigma: Double,
    keyframes: List<List<Number>>,
) : Layer<Double, P> {
    private val timeline: List<Keyframe<P>> = keyframes
        .map { keyframe ->
            require(keyframe.size == SWITCH_OFF || keyframe.size == MOVE_TO) {
                "A keyframe must be either [time] or [time, x, y], but $keyframe was found."
            }
            Keyframe<P>(
                time = keyframe.first().toDouble(),
                source = when (keyframe.size) {
                    MOVE_TO -> BidimensionalGaussianLayer(
                        centerX = keyframe[1].toDouble(),
                        centerY = keyframe[2].toDouble(),
                        norm = norm,
                        sigmaX = sigma,
                    )
                    else -> null
                },
            )
        }.sortedBy { it.time }

    init {
        require(timeline.isNotEmpty()) { "At least one keyframe is required to schedule a source." }
        require(timeline.distinctBy { it.time }.size == timeline.size) {
            "Two keyframes share the same time, the schedule would be ambiguous: $keyframes."
        }
    }

    override fun getValue(p: P): Double = activeSource()?.getValue(p) ?: NO_SOURCE

    private fun activeSource(): BidimensionalGaussianLayer<P>? = environment.simulationTime().let { now ->
        timeline.lastOrNull { it.time <= now }?.source
    }

    override fun toString(): String = "${this::class.simpleName}($timeline)"

    /**
     * The position, if any, held by the source from [time] on.
     */
    private data class Keyframe<P : Position2D<P>>(val time: Double, val source: BidimensionalGaussianLayer<P>?)

    /**
     * Constants for the [ScheduledGaussianLayer].
     */
    companion object {
        /** The size of a keyframe switching the source off. */
        private const val SWITCH_OFF = 1

        /** The size of a keyframe placing the source in a position. */
        private const val MOVE_TO = 3

        /** The value provided where and when no source exists. */
        private const val NO_SOURCE = 0.0

        private const val serialVersionUID: Long = 1L
    }
}
