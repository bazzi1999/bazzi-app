package com.bazzi.app.model

/**
 * Todo数据模型
 */
data class TodoItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "",
    val content: String = "",
    val isCompleted: Boolean = false,
    val createTime: Long = System.currentTimeMillis(),
    val parentId: String? = null,  // 由哪个任务衍生而来
    val childId: String? = null,   // 衍生出了哪个任务
    val isCollapsed: Boolean = false,  // 是否折叠子任务
    val isDeleted: Boolean = false     // 是否已删除（移入历史）
)