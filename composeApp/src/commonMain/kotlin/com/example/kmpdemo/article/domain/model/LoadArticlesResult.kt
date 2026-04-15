package com.example.kmpdemo.article.domain.model

data class LoadArticlesResult(
    val items: List<Article>,
    val source: DataSourceType,
    val isStaleFallback: Boolean = false
)
