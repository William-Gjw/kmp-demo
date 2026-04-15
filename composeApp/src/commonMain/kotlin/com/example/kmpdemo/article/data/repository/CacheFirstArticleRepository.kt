package com.example.kmpdemo.article.data.repository

import com.example.kmpdemo.article.data.local.ArticleEntity
import com.example.kmpdemo.article.data.local.ArticleLocalDataSource
import com.example.kmpdemo.article.data.local.toDomain
import com.example.kmpdemo.article.data.remote.ArticleRemoteDataSource
import com.example.kmpdemo.article.data.remote.toDomain
import com.example.kmpdemo.article.domain.model.DataSourceType
import com.example.kmpdemo.article.domain.model.LoadArticlesResult

class CacheFirstArticleRepository(
    private val localDataSource: ArticleLocalDataSource,
    private val remoteDataSource: ArticleRemoteDataSource,
    private val nowMillis: () -> Long,
    private val cacheTtlMillis: Long = 60_000L
) : ArticleRepository {

    override suspend fun getArticles(forceRefresh: Boolean): LoadArticlesResult {
        val cached = localDataSource.getArticles()
        val cachedResult = cached.toCacheResult(isStaleFallback = false)

        if (!forceRefresh && cachedResult != null && !isCacheExpired()) {
            return cachedResult
        }

        return runCatching {
            val now = nowMillis()
            val remoteItems = remoteDataSource.fetchArticles()
            localDataSource.replaceArticles(
                remoteItems.map { remote ->
                    ArticleEntity(
                        id = remote.id,
                        title = remote.title,
                        summary = remote.summary,
                        updatedAtMillis = remote.updatedAtMillis,
                        cachedAtMillis = now
                    )
                }
            )

            val latest = localDataSource.getArticles()
            LoadArticlesResult(
                items = latest.map { it.toDomain() },
                source = DataSourceType.NETWORK
            )
        }.getOrElse { throwable ->
            if (cachedResult != null) {
                return cachedResult.copy(isStaleFallback = true)
            }
            throw throwable
        }
    }

    override suspend fun clearCache() {
        localDataSource.clearArticles()
    }

    private suspend fun isCacheExpired(): Boolean {
        val cacheTime = localDataSource.getLatestCacheTimeMillis() ?: return true
        return nowMillis() - cacheTime > cacheTtlMillis
    }

    private fun List<ArticleEntity>.toCacheResult(isStaleFallback: Boolean): LoadArticlesResult? {
        if (isEmpty()) {
            return null
        }
        return LoadArticlesResult(
            items = map { it.toDomain() },
            source = DataSourceType.CACHE,
            isStaleFallback = isStaleFallback
        )
    }
}
