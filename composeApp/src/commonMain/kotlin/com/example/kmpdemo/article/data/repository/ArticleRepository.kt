package com.example.kmpdemo.article.data.repository

import com.example.kmpdemo.article.domain.model.LoadArticlesResult

interface ArticleRepository {
    suspend fun getArticles(forceRefresh: Boolean): LoadArticlesResult
    suspend fun clearCache()
}
