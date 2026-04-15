package com.example.kmpdemo.article.data.remote

interface ArticleRemoteDataSource {
    suspend fun fetchArticles(): List<ArticleRemoteDto>
}
