package com.bazzi.app.util

import android.content.Context
import com.bazzi.app.model.TodoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TodoStorage(context: Context) {

    private val prefs = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ---- 当前 Todo 列表 ----
    fun saveTodoList(items: List<TodoItem>) {
        val active = items.filter { !it.isDeleted }
        val json = gson.toJson(active)
        prefs.edit().putString(KEY_TODO_LIST, json).apply()
    }

    fun loadTodoList(): MutableList<TodoItem> {
        val json = prefs.getString(KEY_TODO_LIST, null) ?: return mutableListOf()
        val type = object : TypeToken<List<TodoItem>>() {}.type
        return try {
            gson.fromJson<List<TodoItem>>(json, type).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // ---- 历史记录（已删除的 Todo 系列） ----
    fun saveHistory(items: List<TodoItem>) {
        // 加载已有历史
        val existingHistory = loadHistory()
        // 合并新删除的任务（去重）
        val newDeleted = items.filter { it.isDeleted }
        val merged = (existingHistory + newDeleted).distinctBy { it.id }
        val json = gson.toJson(merged)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun loadHistory(): MutableList<TodoItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return mutableListOf()
        val type = object : TypeToken<List<TodoItem>>() {}.type
        return try {
            gson.fromJson<List<TodoItem>>(json, type).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_TODO_LIST).remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val KEY_TODO_LIST = "todo_list"
        private const val KEY_HISTORY = "todo_history"
    }
}