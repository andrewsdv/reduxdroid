package com.github.s0nerik.reduxdroid.navigation

import androidx.annotation.IdRes

sealed class Nav {
    data class Forward(@IdRes val to: Int) : Nav()
    data class Back(@IdRes val to: Int? = null, val inclusive: Boolean = false) : Nav()
}

sealed class DidNavigate(
        @IdRes open val from: Int,
        @IdRes open val to: Int
) {
    data class Forward(
            @IdRes override val from: Int,
            @IdRes override val to: Int
    ) : DidNavigate(from, to)

    data class Back(
            @IdRes override val from: Int,
            @IdRes override val to: Int
    ) : DidNavigate(from, to)
}