package com.github.s0nerik.reduxdroid.core

import com.github.s0nerik.reduxdroid.core.di.ActionConverter
import com.github.s0nerik.reduxdroid.core.di.ActionConverterHolder
import me.tatarka.redux.middleware.Middleware
import org.koin.standalone.KoinComponent
import kotlin.reflect.KClass

private data class ConvertersHolder(
        val dropOriginalAction: Boolean,
        val converters: List<ActionConverter<Any, Any>>
)

internal class ActionConverterMiddleware(
        converters: Map<KClass<*>, List<ActionConverterHolder<Any, Any>>>
) : Middleware<Any, Any>, KoinComponent {
    private val converterHolders: Map<KClass<*>, ConvertersHolder> = converters.mapValues {
        ConvertersHolder(
                dropOriginalAction = it.value.asIterable().map { it.dropOriginalAction }.contains(true),
                converters = it.value.map { it.converter }
        )
    }

    override fun dispatch(next: Middleware.Next<Any, Any>, action: Any): Any {
        val clazz = action::class
        val holder = converterHolders[clazz]
        if (holder != null) {
            var resultAction: Any = Unit

            if (!holder.dropOriginalAction)
                resultAction = next.next(action)

            holder.converters.forEach { converter ->
                converter(action)?.let {
                    resultAction = next.next(it)
                }
            }
            return resultAction
        } else {
            return next.next(action)
        }
    }
}