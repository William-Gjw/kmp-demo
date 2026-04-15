package com.example.kmpdemo.article.data.remote

import com.example.kmpdemo.article.domain.model.Article

data class ArticleRemoteDto(
    val id: String,
    val title: String,
    val summary: String,
    val updatedAtMillis: Long
)

fun ArticleRemoteDto.toDomain(): Article =
    Article(
        id = id,
        title = title,
        summary = summary,
        updatedAtMillis = updatedAtMillis
    )
