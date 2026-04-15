package com.example.kmpdemo.article.data.local

import com.example.kmpdemo.article.domain.model.Article

data class ArticleEntity(
    val id: String,
    val title: String,
    val summary: String,
    val updatedAtMillis: Long,
    val cachedAtMillis: Long
)

fun ArticleEntity.toDomain(): Article =
    Article(
        id = id,
        title = title,
        summary = summary,
        updatedAtMillis = updatedAtMillis
    )
