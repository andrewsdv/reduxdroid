package com.github.s0nerik.reduxdroid.core.di

import com.github.s0nerik.reduxdroid.core.*
import com.github.s0nerik.reduxdroid.core.middleware.Middleware
import com.github.s0nerik.reduxdroid.core.middleware.ReduxMiddleware
import me.tatarka.redux.Dispatcher
import org.koin.core.definition.DefinitionContext

/**
 * Provides ActionDispatcher with such options:
 * - action converter
 * - apply application-registered middlewares
 *
 * This function should *NOT* be used in a client code directly. Use [ActionDispatcher] provided by Koin.
 */
fun DefinitionContext.actionDispatcher(
        store: StateStore,
        withActionConverter: Boolean = true,
        applyAppMiddlewares: Boolean = true,
        extraMiddlewares: List<Middleware<Any, Any>> = emptyList()
): ActionDispatcher {
    val actionConverterMiddleware = ActionConverterMiddleware(actionConverters)

    val middlewares = mutableListOf<Middleware<Any, Any>>()

    if (withActionConverter)
        middlewares += actionConverterMiddleware

    if (applyAppMiddlewares)
        middlewares += appMiddlewaresProvider()

    middlewares += extraMiddlewares

    val dispatcher = Dispatcher.forStore(store.store, _combinedReducer())
            .chain(middlewares.map { ReduxMiddleware(it) }.reversed())

    return ActionDispatcherImpl(dispatcher)
}