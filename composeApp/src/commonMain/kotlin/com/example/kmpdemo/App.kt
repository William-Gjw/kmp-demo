package com.example.kmpdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.kmpdemo.mvi.HelloEffect
import com.example.kmpdemo.mvi.HelloIntent
import com.example.kmpdemo.mvi.HelloViewModel

@Composable
fun App() {
    val viewModel = remember { HelloViewModel() }
    val state by viewModel.state.collectAsState()
    var latestEffect by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { viewModel.clear() }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HelloEffect.MessageLoaded -> latestEffect = effect.message
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("KMP + MVI Demo")
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(text = state.message)
                }

                Button(
                    onClick = { viewModel.dispatch(HelloIntent.RefreshHello) }
                ) {
                    Text("Refresh")
                }

                latestEffect?.let {
                    Text("Effect: $it")
                }
            }
        }
    }
}
