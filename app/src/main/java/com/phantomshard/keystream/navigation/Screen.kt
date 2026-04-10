package com.phantomshard.keystream.navigation

import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    data object SignIn : Screen()

    @Serializable
    data object Dashboard : Screen()
}
