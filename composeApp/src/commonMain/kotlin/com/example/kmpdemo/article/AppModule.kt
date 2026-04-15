package com.example.kmpdemo.article

import com.example.kmpdemo.article.data.local.InMemoryArticleLocalDataSource
import com.example.kmpdemo.article.data.remote.FakeArticleRemoteDataSource
import com.example.kmpdemo.article.data.repository.CacheFirstArticleRepository
import com.example.kmpdemo.article.platform.currentTimeMillis
import com.example.kmpdemo.article.presentation.ArticleViewModel

fun createArticleViewModelForDemo(): ArticleViewModel {
    val local = InMemoryArticleLocalDataSource()
    val remote = FakeArticleRemoteDataSource()
    val repository = CacheFirstArticleRepository(
        localDataSource = local,
        remoteDataSource = remote,
        nowMillis = ::currentTimeMillis,
        cacheTtlMillis = 15_000L
    )

    return ArticleViewModel(repository = repository)
}
