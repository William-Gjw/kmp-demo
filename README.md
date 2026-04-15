# kmp-demo

Kotlin Multiplatform (KMP) + Compose Desktop 演示项目。

当前示例聚焦一个更真实的 MVI 场景：

- 首次进入先读缓存
- 缓存过期或为空时回源网络
- 网络成功后写回缓存
- 支持强制刷新、删除缓存
- UI 通过 `StateFlow/SharedFlow` 响应状态与一次性事件

## 关键学习文档

- `docs/kmp-mvi-cache-demo-guide.md`
  - A 部分：教程式讲解数据流转
  - B 部分：工程落地目录和代码骨架

## 运行方式

```bash
./gradlew :composeApp:run
```
