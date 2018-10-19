package com.github.s0nerik.reduxdroid.core.di

import org.koin.dsl.context.ModuleDefinition
import kotlin.reflect.KClass

@PublishedApi
internal val ACTION_CONVERTERS_KEY = "_actionConverters"

@PublishedApi
internal val NON_UNIQUE_ACTION_CONVERTERS_KEY = "_nonUniqueActionConverters"

typealias ActionConverter<A1, A2> = (A1) -> A2?

@PublishedApi
internal data class ActionConverterHolder<A1, A2>(
        val dropOriginalAction: Boolean,
        val converter: ActionConverter<A1, A2>
)

@PublishedApi
internal inline fun <A1, A2> _filteredActionConverter(
        crossinline converter: ActionConverter<A1, A2>,
        crossinline filter: (A1) -> Boolean
): ActionConverter<A1, A2> = { action: A1 ->
    if (filter(action))
        converter(action)
    else
        null
}

internal val ModuleDefinition.actionConverters
    get() = getUniqueKeyMap<KClass<*>, ActionConverterHolder<Any, Any>>(ACTION_CONVERTERS_KEY)

internal val ModuleDefinition.nonUniqueActionConverters
    get() = getNonUniqueKeyMap<KClass<*>, ActionConverterHolder<Any, Any>>(NON_UNIQUE_ACTION_CONVERTERS_KEY)

/**
 * Registers the action converter to be used when action [A] gets dispatched.
 * Every time the action [A] gets dispatched - it will be mapped to the specified action and passed downstream.
 * Registering a reducer for the same action type more than once using this method is prohibited.
 *
 * @see [ModuleDefinition.actionConverter]
 */
inline fun <reified A> ModuleDefinition.actionConverter(
        dropOriginalAction: Boolean = true,
        noinline converter: ActionConverter<A, Any>
) {
    if (dropOriginalAction) {
        addUniqueKeyMapEntry(
                propertyName = ACTION_CONVERTERS_KEY,
                itemKey = A::class,
                itemValue = ActionConverterHolder(dropOriginalAction, converter),
                duplicateKeyError = "Only one ActionConverter(dropOriginalAction = true) can be registered. You still can register multiple filtered ActionConverters or ActionConverter(dropOriginalAction = false) for this action. Action type: ${A::class}"
        )
    } else {
        addNonUniqueKeyMapEntry(
                propertyName = NON_UNIQUE_ACTION_CONVERTERS_KEY,
                itemKey = A::class,
                itemValue = ActionConverterHolder(dropOriginalAction, converter)
        )
    }
}

/**
 * Registers the action converter to be used when action [A] gets dispatched.
 * Every time the action [A] gets dispatched - it will be mapped to the specified action and passed downstream, but
 * only if [filter] returns true, otherwise original action would be dispatched.
 * Registering a reducer for the same action type more than once using this method is prohibited.
 *
 * @see [ModuleDefinition.reducer]
 */
inline fun <reified A> ModuleDefinition.actionConverter(
        dropOriginalAction: Boolean = true,
        crossinline converter: ActionConverter<A, Any>,
        crossinline filter: (A) -> Boolean
) = addNonUniqueKeyMapEntry(
        propertyName = NON_UNIQUE_ACTION_CONVERTERS_KEY,
        itemKey = A::class,
        itemValue = ActionConverterHolder(dropOriginalAction, _filteredActionConverter(converter, filter))
)