package com.example.kmpdemo.article.data.remote

import com.example.kmpdemo.article.platform.currentTimeMillis
import kotlinx.coroutines.delay

class FakeArticleRemoteDataSource : ArticleRemoteDataSource {
    private var requestCount: Int = 0

    override suspend fun fetchArticles(): List<ArticleRemoteDto> {
        delay(800)
        requestCount += 1
        val now = currentTimeMillis()
        val version = requestCount
        return listOf(
            ArticleRemoteDto(
                id = "1",
                title = "KMP 缓存策略解析 v$version",
                summary = "第 $version 次网络返回：先缓存后网络是 Offline-first 的核心。",
                updatedAtMillis = now
            ),
            ArticleRemoteDto(
                id = "2",
                title = "MVI 数据流实践 v$version",
                summary = "Intent -> ViewModel -> Repository -> State 的闭环。",
                updatedAtMillis = now
            ),
            ArticleRemoteDto(
                id = "3",
                title = "强刷与删缓存 v$version",
                summary = "强刷跳过缓存，删缓存清空本地并重置界面。",
                updatedAtMillis = now
            )
        )
    }
}
