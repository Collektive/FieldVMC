package it.unibo.collektive.coordination

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.exchanging
import it.unibo.collektive.stdlib.collapse.fold
import it.unibo.collektive.stdlib.accumulation.convergeCast
import it.unibo.collektive.stdlib.accumulation.findParent

@PublishedApi
internal inline fun <reified ID : Comparable<ID>> Aggregate<ID>.findDisambiguatedParent(
    potential: Double,
    crossinline disambiguateParent: (ID, ID) -> ID,
): ID =
    findParent(
        potential,
        { (id1, _), (id2, _) ->
            when (disambiguateParent(id1, id2)) {
                id1 -> -1
                id2 -> 1
                else -> error("Impossible to disambiguate parent $id2 and $id1")
            }
        },
    )

/**
 * Aggregate a field of type T within a spanning tree built according to the maximum
 * decrease in [potential].
 * Accumulate the [potential] according to the [reduce] function.
 * [local] is the value field providing the value to be collected for each device.
 */
inline fun <reified T, reified ID> Aggregate<ID>.convergeCast(
    potential: Double,
    local: T,
    noinline disambiguateParent: (ID, ID) -> ID = { a, b -> minOf(a, b) },
    crossinline reduce: (T, T) -> T,
): T where ID : Comparable<ID> =
    convergeCast(
        local = local,
        potential = potential,
        selectParent = { (id1, _), (id2, _) ->
            when (disambiguateParent(id1, id2)) {
                id1 -> -1
                id2 -> 1
                else -> error("Impossible to disambiguate parent $id2 and $id1")
            }
        },
        accumulateData = reduce,
    )


/**
 * Spreads the [localResource] to the children of this node, according to the [localSuccess] of each child.
 */
inline fun <reified ID> Aggregate<ID>.spreadToChildren(
    env: EnvironmentVariables,
    potential: Double,
    localResource: Double,
    localSuccess: Double,
    noinline disambiguateParent: (ID, ID) -> ID = { a, b -> minOf(a, b) },
): Double where ID : Comparable<ID> =
    exchanging(localResource) { resource ->
        val parent = findDisambiguatedParent(potential, disambiguateParent) // the parent of this device
        val myLocalResources =
            resource.neighbors
                .fold(localResource) { accumulator, neighborResource ->
                    if (neighborResource.id == parent) {
                        accumulator + neighborResource.value
                    } else {
                        accumulator
                    }
                }
        val neighborParents = neighboring(parent) // Each device is mapped to its parent
        val childrenSuccess: Field<ID, Double> =
            neighborParents
                .alignedMap(neighboring(localSuccess)) { _, itsParent, itsSuccess ->
                    when {
                        itsParent == localId -> itsSuccess
                        else -> 0.0
                    }
                }
        val selfConsumption =
            myLocalResources / childrenSuccess.neighbors.fold(1) { a, b ->
                a + if (b.value > 0) 1 else 0
            }
        if (potential > 0.0) env["resource"] = selfConsumption
        val resourcesToSpread = myLocalResources - selfConsumption
        val overallChildrenSuccess =
            childrenSuccess.neighbors.fold(0.0) { accumulator, childSuccess ->
                accumulator + childSuccess.value
            }
        childrenSuccess
            .map { if (overallChildrenSuccess <= 0) 0.0 else it.value * resourcesToSpread / overallChildrenSuccess }
            .yielding {
                neighboring(myLocalResources)
            }
    }.local.value

