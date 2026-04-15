# kmp-demo

最基础的 Kotlin Multiplatform (KMP) + Compose Desktop 示例，使用标准 MVI 模式实现 `Hello World` 展示。

## 架构说明（MVI）

- **Intent**：`HelloIntent`，用于表达用户动作（首次加载、刷新）
- **State**：`HelloState`，描述界面状态（是否加载中、展示文本）
- **Effect**：`HelloEffect`，一次性事件（内容加载完成）
- **ViewModel**：`HelloViewModel`，处理 Intent、更新 State、发送 Effect
- **View**：`App()`，订阅 State 渲染界面并分发 Intent

## 运行方式

```bash
./gradlew :composeApp:run
```
