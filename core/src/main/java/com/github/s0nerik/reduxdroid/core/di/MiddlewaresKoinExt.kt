package com.github.s0nerik.reduxdroid.core.di

import me.tatarka.redux.middleware.Middleware
import org.koin.dsl.context.ModuleDefinition

@PublishedApi
internal val MIDDLEWARES_KEY = "MIDDLEWARES_KEY"

/**
 * Registers a list of custom middlewares to be used in the app. In logical order.
 */
fun ModuleDefinition.middlewares(middlewaresProvider: () -> List<Middleware<*, *>>) {
    if (koinContext.getProperty<Any?>(MIDDLEWARES_KEY, null) != null) {
        error("Middlewares can only be registered once. Consider placing middleware registration logic into a main application module.")
    }
    koinContext.setProperty(MIDDLEWARES_KEY, middlewaresProvider)
}

internal val ModuleDefinition.appMiddlewares: List<Middleware<Any, Any>>
        get() = koinContext.getProperty<() -> List<Middleware<Any, Any>>>(MIDDLEWARES_KEY, { emptyList() })()