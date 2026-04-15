package com.example.kmpdemo.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelloViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow(HelloState())
    val state: StateFlow<HelloState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HelloEffect>()
    val effect: SharedFlow<HelloEffect> = _effect.asSharedFlow()

    init {
        dispatch(HelloIntent.LoadHello)
    }

    fun dispatch(intent: HelloIntent) {
        when (intent) {
            HelloIntent.LoadHello,
            HelloIntent.RefreshHello -> loadHelloWorld()
        }
    }

    fun clear() {
        scope.cancel()
    }

    private fun loadHelloWorld() {
        scope.launch {
            val hello = "Hello World"
            _state.value = HelloState(isLoading = false, message = hello)
            _effect.emit(HelloEffect.MessageLoaded(hello))
        }
    }
}
