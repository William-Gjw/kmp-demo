package com.example.kmpdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpdemo.article.createArticleViewModelForDemo
import com.example.kmpdemo.article.presentation.ArticleEffect
import com.example.kmpdemo.article.presentation.ArticleIntent

@Composable
fun App() {
    val viewModel = remember { createArticleViewModelForDemo() }
    val state by viewModel.state.collectAsState()
    var latestEffect by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { viewModel.clear() }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ArticleEffect.ShowMessage -> latestEffect = effect.message
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("KMP + MVI 缓存优先 Demo", style = MaterialTheme.typography.titleLarge)
                Text("数据来源: ${state.source ?: "-"}")
                if (state.isStaleFallback) {
                    Text("提示：当前展示的是网络失败后的缓存兜底数据")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.dispatch(ArticleIntent.ForceRefresh) }) {
                        Text("强制刷新")
                    }
                    Button(onClick = { viewModel.dispatch(ArticleIntent.ClearCache) }) {
                        Text("删除缓存")
                    }
                }

                when {
                    state.isLoading -> CircularProgressIndicator()
                    state.errorMessage != null -> Text("错误：${state.errorMessage}")
                    state.isEmpty -> Text("暂无数据，请点击强制刷新")
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.items) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(item.summary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                latestEffect?.let { message ->
                    Text("Effect: $message")
                }
            }
        }
    }
}
