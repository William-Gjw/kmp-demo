package com.example.kmpdemo.article.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryArticleLocalDataSource : ArticleLocalDataSource {
    private val mutex = Mutex()
    private var cache: List<ArticleEntity> = emptyList()

    override suspend fun getArticles(): List<ArticleEntity> =
        mutex.withLock { cache }

    override suspend fun replaceArticles(items: List<ArticleEntity>) {
        mutex.withLock {
            cache = items
        }
    }

    override suspend fun clearArticles() {
        mutex.withLock {
            cache = emptyList()
        }
    }

    override suspend fun getLatestCacheTimeMillis(): Long? =
        mutex.withLock { cache.maxOfOrNull { it.cachedAtMillis } }
}
