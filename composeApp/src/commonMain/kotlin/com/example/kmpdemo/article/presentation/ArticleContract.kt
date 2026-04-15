package com.example.kmpdemo.article.presentation

import com.example.kmpdemo.article.domain.model.DataSourceType

sealed interface ArticleIntent {
    data object LoadInitial : ArticleIntent
    data object ForceRefresh : ArticleIntent
    data object ClearCache : ArticleIntent
}

data class ArticleUiModel(
    val id: String,
    val title: String,
    val summary: String
)

data class ArticleState(
    val isLoading: Boolean = false,
    val items: List<ArticleUiModel> = emptyList(),
    val source: DataSourceType? = null,
    val isStaleFallback: Boolean = false,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty() && errorMessage == null
}

sealed interface ArticleEffect {
    data class ShowMessage(val message: String) : ArticleEffect
}
