package com.example.kmpdemo.article.domain.model

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val updatedAtMillis: Long
)
