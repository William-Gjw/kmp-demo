package com.example.kmpdemo.article.data.local

interface ArticleLocalDataSource {
    suspend fun getArticles(): List<ArticleEntity>
    suspend fun replaceArticles(items: List<ArticleEntity>)
    suspend fun clearArticles()
    suspend fun getLatestCacheTimeMillis(): Long?
}
