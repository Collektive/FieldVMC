package it.unibo.collektive.alchemist.device.sensors.impl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.nextUp

/**
 * An Alchemist [NodeProperty] that allows a node to spawn new nodes or destroy itself.
 *
 * @param T The concentration type of the node.
 * @param P The position type of the environment.
 * @param randomGenerator The random engine used for adding jitter to spawn times.
 * @param environment The simulation environment.
 * @property node The Alchemist node this property is attached to.
 * @property cloningRange The fixed distance from the node at which a new spawn occurs.
 * @property maxChildren The maximum number of children this node is allowed to spawn.
 * @property minSpawnWait The minimum amount of time to wait before a new spawn is allowed.
 */
class DeviceSpawner<T, P : Position<P>>
@JvmOverloads
constructor(
    private val randomGenerator: RandomGenerator,
    private val environment: Environment<T, P>,
    override val node: Node<T>,
    override val cloningRange: Double = 1.0,
    override val maxChildren: Int,
    override val minSpawnWait: Double = 20.0,
) : DeviceSpawn,
    NodeProperty<T> {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> =
        DeviceSpawner(randomGenerator, environment, node, cloningRange, maxChildren, minSpawnWait)

    @Suppress("UNCHECKED_CAST")
    override fun spawn(coordinate: Pair<Double, Double>): Double {
        val spawningTime =
            environment.simulation.time + DoubleTime(randomGenerator.nextDouble(0.0.nextUp(), MAX_SPAWN_JITTER))
        val cloneOfThis = node.cloneNode(spawningTime)
        cloneOfThis.setConcentration(SimpleMolecule("leader"), false as T)
        val updatedPosition = environment.makePosition(*coordinate.toList().toTypedArray())
        environment.addNode(cloneOfThis, updatedPosition)
        return spawningTime.toDouble()
    }

    override fun selfDestroy() {
        node.reactions.toList().forEach {
            environment.simulation.reactionRemoved(it)
            node.removeReaction(it)
        }
        environment.simulation.schedule { environment.removeNode(node) }
    }

    override fun currentTime(): Double = environment.simulation.time.toDouble()

    /**
     * Constants for the [DeviceSpawner] property.
     */
    companion object {
        /**
         * Maximum time jitter added to a node's spawn time.
         */
        private const val MAX_SPAWN_JITTER = 0.1
    }
}
