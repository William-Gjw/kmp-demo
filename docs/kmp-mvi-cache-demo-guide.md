# KMP + MVI 新手实战：缓存优先 + 网络回源 + 强制刷新 + 删除缓存（Android / iOS）

> 目标读者：有 Android 基础，但刚接触 KMP 与 MVI 的开发者。  
> 目标结果：你能完整理解并自己实现一套“先读缓存，缓存失效再请求网络”的跨平台数据流转。

---

## A. 教程版（先讲懂数据流转）

### A-1. 你要做的需求（翻译成工程语言）

你描述的需求是：

1. 页面启动后先读本地缓存（数据库）
2. 缓存不存在或过期 -> 请求网络
3. 网络成功后：
   - 更新页面
   - 写入数据库（作为新缓存）
4. 用户可以点击：
   - 强制刷新（跳过缓存，直接网络）
   - 删除缓存
5. Android 和 iOS 共用核心业务逻辑（KMP 共享层）

这就是典型的 **Offline-First（离线优先）** 设计，配合 MVI 非常合适。

---

### A-2. 先记住这 5 个角色（非常关键）

从 MVI + Clean Architecture 视角，你可以这样分层：

1. **View（UI）**  
   只负责：
   - 展示 `State`
   - 把用户点击变成 `Intent`

2. **ViewModel / Store（MVI 核心）**  
   只负责：
   - 接收 `Intent`
   - 调用 UseCase / Repository
   - 产出新的 `State`
   - 产出一次性 `Effect`（比如 Toast）

3. **Repository（数据决策层）**  
   只负责：
   - 决定“读缓存还是读网络”
   - 持久化缓存

4. **LocalDataSource（数据库）**  
   只负责 CRUD（查、存、删）

5. **RemoteDataSource（网络）**  
   只负责发请求拿数据

> 初学者最容易错的点：把“读缓存还是读网络”的逻辑写进 UI。  
> 正确做法：让 Repository 统一做决策。

---

### A-3. 一个完整数据流（页面首次进入）

我们用“加载文章列表”举例。

#### 1）UI 发出 Intent

页面首次展示时发出：

- `Intent.LoadInitial`

#### 2）ViewModel 收到 Intent

ViewModel 先把状态改为 loading（可选）：

- `State(isLoading = true)`

然后调用：

- `repository.getArticles(forceRefresh = false)`

#### 3）Repository 执行缓存策略

Repository 内部流程：

1. 读数据库 `local.getArticles()`
2. 如果本地有数据并且未过期 -> 直接返回本地
3. 否则请求网络 `remote.fetchArticles()`
4. 网络成功后写库 `local.upsertArticles(...)`
5. 返回最新数据

#### 4）ViewModel 更新 State

- `State(isLoading = false, articles = [...], source = CACHE/NETWORK)`

#### 5）UI 自动重绘

UI 只需要 `collect state`，拿到新状态后自动显示列表。

---

### A-4. 用户点击“强制刷新”的流转

#### Intent

- `Intent.ForceRefresh`

#### Repository 策略变化

- 直接忽略缓存，请求网络
- 网络成功后覆盖本地缓存
- 返回网络最新数据

#### UI 结果

- 列表更新为最新内容
- 可触发 `Effect.ShowToast("刷新成功")`

---

### A-5. 用户点击“删除缓存”的流转

#### Intent

- `Intent.ClearCache`

#### Repository 动作

- `local.clearArticles()`

#### ViewModel 处理

- 更新 State：列表清空或显示空态
- 发出 Effect：`Effect.ShowToast("缓存已清除")`

---

### A-6. MVI 三件套怎么设计（最小模板）

```kotlin
sealed interface ArticleIntent {
    data object LoadInitial : ArticleIntent
    data object ForceRefresh : ArticleIntent
    data object ClearCache : ArticleIntent
}

data class ArticleState(
    val isLoading: Boolean = false,
    val items: List<ArticleUiModel> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
    val lastSource: DataSourceType? = null // CACHE or NETWORK
)

sealed interface ArticleEffect {
    data class ShowToast(val message: String) : ArticleEffect
}
```

---

### A-7. Repository 缓存策略（最重要）

建议统一为一个入口：

```kotlin
suspend fun getArticles(forceRefresh: Boolean): List<Article>
```

伪代码：

```kotlin
override suspend fun getArticles(forceRefresh: Boolean): List<Article> {
    if (!forceRefresh) {
        val cached = local.getArticles()
        if (cached.isNotEmpty() && !isExpired(cached.first().cachedAt)) {
            return cached
        }
    }

    val remoteItems = remote.fetchArticles()
    val now = clock.nowMillis()
    local.replaceArticles(remoteItems.map { it.toEntity(cachedAt = now) })
    return local.getArticles()
}
```

---

### A-8. 过期策略怎么定（简单可靠）

在每条缓存记录或单独 metadata 表中保存：

- `cachedAt`（毫秒时间戳）

过期判断：

```kotlin
val expired = (now - cachedAt) > cacheTtlMillis
```

比如：

- `cacheTtlMillis = 5 * 60 * 1000L`（5 分钟）

---

### A-9. 为什么这种写法适合 KMP

因为 Android 与 iOS 共用的是：

- Intent / State / Effect
- ViewModel / Store
- Repository 策略
- DataSource 接口

平台层只做实现细节差异：

- SQLDelight 驱动（AndroidSqliteDriver / NativeSqliteDriver）
- 网络引擎（OkHttp / Darwin）
- UI 容器（Android Compose / iOS SwiftUI）

---

### A-10. 一句话记住数据流

**UI 不直接碰网络和数据库，UI 只发 Intent；  
ViewModel 只改 State/发 Effect；  
Repository 负责“缓存 vs 网络”的决策；  
DataSource 负责执行读写。**

---

## B. 工程实战版（你可以按这个目录写 demo）

下面给你一个“可落地”的 demo 结构（KMP 共享层 + Android/iOS UI）。

### B-1. 推荐技术栈（业界常用）

- **KMP**：共享业务层
- **Compose Multiplatform / Android Compose + SwiftUI**：界面层（任选）
- **Ktor Client**：网络
- **SQLDelight**：跨平台数据库
- **kotlinx.coroutines + Flow**：异步与状态流
- **kotlinx.datetime**：跨平台时间
- **Koin 或手写 DI**：依赖注入（demo 可先手写）

---

### B-2. 目录建议

```text
shared/
  src/commonMain/
    data/
      remote/
      local/
      repository/
    domain/
      model/
      usecase/
    presentation/
      mvi/
  src/androidMain/
    data/local/driver/
    data/remote/engine/
  src/iosMain/
    data/local/driver/
    data/remote/engine/
androidApp/
iosApp/
```

---

### B-3. Domain 模型

```kotlin
data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val updatedAt: Long
)
```

---

### B-4. Data 接口定义

```kotlin
interface ArticleRemoteDataSource {
    suspend fun fetchArticles(): List<ArticleDto>
}

interface ArticleLocalDataSource {
    suspend fun getArticles(): List<ArticleEntity>
    suspend fun replaceArticles(items: List<ArticleEntity>)
    suspend fun clearArticles()
    suspend fun getLastCacheTime(): Long?
}
```

---

### B-5. Repository 接口与实现

```kotlin
interface ArticleRepository {
    suspend fun getArticles(forceRefresh: Boolean): DataResult
    suspend fun clearCache()
}

data class DataResult(
    val items: List<Article>,
    val source: DataSourceType
)

enum class DataSourceType { CACHE, NETWORK }
```

实现要点：

1. 非强刷：先尝试缓存
2. 缓存可用且未过期：返回 CACHE
3. 否则拉网，写库，再返回 NETWORK
4. 失败时：
   - 若有旧缓存可兜底，返回旧缓存 + 提示 stale
   - 若无缓存，抛错给上层展示错误态

---

### B-6. MVI 合约（Intent/State/Effect）

```kotlin
sealed interface ArticleIntent {
    data object LoadInitial : ArticleIntent
    data object ForceRefresh : ArticleIntent
    data object ClearCache : ArticleIntent
}

data class ArticleState(
    val isLoading: Boolean = false,
    val items: List<ArticleUiModel> = emptyList(),
    val error: String? = null,
    val isEmpty: Boolean = false,
    val source: DataSourceType? = null
)

sealed interface ArticleEffect {
    data class ShowMessage(val text: String) : ArticleEffect
}
```

---

### B-7. ViewModel（核心逻辑）

伪代码结构：

```kotlin
class ArticleViewModel(
    private val repository: ArticleRepository
) {
    val state: StateFlow<ArticleState>
    val effect: SharedFlow<ArticleEffect>

    fun dispatch(intent: ArticleIntent) {
        when (intent) {
            LoadInitial -> load(forceRefresh = false)
            ForceRefresh -> load(forceRefresh = true)
            ClearCache -> clearCache()
        }
    }

    private fun load(forceRefresh: Boolean) { /* 调 repository，更新 state/effect */ }
    private fun clearCache() { /* 调 repository.clearCache */ }
}
```

---

### B-8. SQLDelight 表设计建议

```sql
CREATE TABLE article (
  id TEXT NOT NULL PRIMARY KEY,
  title TEXT NOT NULL,
  summary TEXT NOT NULL,
  updated_at INTEGER NOT NULL,
  cached_at INTEGER NOT NULL
);

CREATE TABLE cache_meta (
  key TEXT NOT NULL PRIMARY KEY,
  value INTEGER NOT NULL
);
```

你可以只用 `article.cached_at`，也可以额外维护 `cache_meta.last_sync_time`。

---

### B-9. 网络层建议（Ktor）

`commonMain` 定义 API；  
平台层注入引擎：

- Android：OkHttp
- iOS：Darwin

解析后转 DTO -> Entity/Domain。

---

### B-10. UI 层如何接 MVI（Android / iOS）

#### Android（Compose）

1. `collectAsState()` 订阅 `state`
2. `LaunchedEffect` 收集 `effect`（Toast/Snackbar）
3. 点击事件 -> `dispatch(Intent)`

#### iOS（SwiftUI）

1. 通过桥接把 `StateFlow` 暴露给 Swift（如 KMP-NativeCoroutines）
2. 按钮触发 `dispatch`
3. `effect` 用于弹提示

---

### B-11. Demo 开发顺序（推荐你照抄）

1. 先写 `Intent/State/Effect`
2. 写 Repository 接口 + 假实现（先不接真实网和库）
3. 把 UI 跑通（先看见状态变化）
4. 接入 Ktor 远程数据源
5. 接入 SQLDelight 本地数据源
6. 实现缓存过期策略
7. 加“强制刷新/删除缓存”
8. 补测试（Repository + ViewModel）

---

### B-12. 测试清单（必须）

至少覆盖：

1. 首次进入无缓存 -> 走网络 -> 落库成功
2. 有缓存且未过期 -> 不请求网络
3. 有缓存但过期 -> 请求网络并更新缓存
4. 强制刷新 -> 一定请求网络
5. 删除缓存 -> 本地为空
6. 网络失败但有旧缓存 -> 可兜底显示
7. 网络失败且无缓存 -> 显示错误态

---

### B-13. 新手最常见 6 个坑

1. 在 UI 里直接写网络/数据库逻辑
2. `State` 和 `Effect` 不分，导致重复弹 Toast
3. 缓存过期时间写死在 UI 层
4. 强刷逻辑忘记跳过缓存
5. 网络成功后忘记回写数据库
6. 错误状态不清理，导致 UI 一直显示旧错误

---

## 下一步你可以怎么练习

你可以按下面节奏练：

1. 先只实现 3 个 Intent（LoadInitial / ForceRefresh / ClearCache）
2. 只做一个页面（文章列表）
3. 先 mock 数据跑通 MVI，再接 Ktor + SQLDelight
4. 最后补测试，把数据流转“跑明白”

当你把这个 demo 跑通，你对 KMP + MVI 的理解会非常扎实。

