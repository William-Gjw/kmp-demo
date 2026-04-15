package com.example.kmpdemo.mvi

sealed interface HelloIntent {
    data object LoadHello : HelloIntent
    data object RefreshHello : HelloIntent
}

data class HelloState(
    val isLoading: Boolean = true,
    val message: String = ""
)

sealed interface HelloEffect {
    data class MessageLoaded(val message: String) : HelloEffect
}
