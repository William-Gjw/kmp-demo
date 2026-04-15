package com.example.kmpdemo.article.presentation

import com.example.kmpdemo.article.data.repository.ArticleRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticleViewModel(
    private val repository: ArticleRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow(ArticleState())
    val state: StateFlow<ArticleState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ArticleEffect>()
    val effect: SharedFlow<ArticleEffect> = _effect.asSharedFlow()

    init {
        dispatch(ArticleIntent.LoadInitial)
    }

    fun dispatch(intent: ArticleIntent) {
        when (intent) {
            ArticleIntent.LoadInitial -> load(forceRefresh = false)
            ArticleIntent.ForceRefresh -> load(forceRefresh = true)
            ArticleIntent.ClearCache -> clearCache()
        }
    }

    fun clear() {
        scope.cancel()
    }

    private fun load(forceRefresh: Boolean) {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.getArticles(forceRefresh) }
                .onSuccess { result ->
                    _state.value = ArticleState(
                        isLoading = false,
                        items = result.items.map { article ->
                            ArticleUiModel(
                                id = article.id,
                                title = article.title,
                                summary = article.summary
                            )
                        },
                        source = result.source,
                        isStaleFallback = result.isStaleFallback
                    )

                    when {
                        result.isStaleFallback -> _effect.emit(
                            ArticleEffect.ShowMessage("网络失败，已展示缓存数据")
                        )

                        forceRefresh -> _effect.emit(
                            ArticleEffect.ShowMessage("强制刷新成功")
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "加载失败，请重试"
                        )
                    }
                    _effect.emit(ArticleEffect.ShowMessage("加载失败"))
                }
        }
    }

    private fun clearCache() {
        scope.launch {
            repository.clearCache()
            _state.value = ArticleState()
            _effect.emit(ArticleEffect.ShowMessage("缓存已删除"))
        }
    }
}
