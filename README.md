# bazzi-app

一个轻量级的 Android Todo 任务管理应用，支持任务关联（衍生子任务）、历史记录查看。

## 功能特性

- **任务管理**：添加、编辑、完成、删除 Todo 任务
- **描述备注**：为每个任务添加可选的文字描述，小字显示在标题下方
- **任务衍生**：从任意任务（主任务或子任务）衍生出新的子任务，形成任务树形结构
- **折叠展开**：有子任务的父任务可折叠/展开子任务列表
- **历史记录**：删除的任务系列归档到历史页，可按日期筛选查看
- **简约 UI**：浅蓝色主题背景，无顶部 ActionBar，FAB 加号添加任务

## 技术栈

- Kotlin
- Jetpack AppCompat / Material3
- ConstraintLayout / RecyclerView
- Gson 本地存储（SharedPreferences）

## 版本

**当前版本：1.1.0**