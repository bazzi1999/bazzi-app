package com.bazzi.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bazzi.app.adapter.TodoAdapter
import com.bazzi.app.model.TodoItem
import com.bazzi.app.util.TodoStorage
import com.google.android.material.button.MaterialButton

class TodoListActivity : AppCompatActivity() {

    private val todoList = mutableListOf<TodoItem>()
    private lateinit var adapter: TodoAdapter
    private lateinit var etNewTodo: EditText
    private lateinit var btnAdd: MaterialButton
    private lateinit var rvTodos: RecyclerView
    private lateinit var storage: TodoStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "TodoList"

        // 右上角历史按钮
        supportActionBar?.setDisplayShowCustomEnabled(true)
        val historyBtn = MaterialButton(this).apply {
            text = "📋 历史"
            textSize = 14f
            setOnClickListener {
                startActivity(Intent(this@TodoListActivity, HistoryActivity::class.java))
            }
        }
        supportActionBar?.customView = historyBtn

        // 初始化存储
        storage = TodoStorage(this)

        etNewTodo = findViewById(R.id.etNewTodo)
        btnAdd = findViewById(R.id.btnAdd)
        rvTodos = findViewById(R.id.rvTodos)

        // 加载已保存的活跃数据
        todoList.addAll(storage.loadTodoList())

        adapter = TodoAdapter(
            items = todoList,
            onToggleCompleted = { updatedItem ->
                updateItem(updatedItem)
            },
            onDeriveTask = { parentItem ->
                showDeriveDialog(parentItem)
            },
            onToggleCollapse = { itemId ->
                toggleCollapse(itemId)
            },
            onDeleteSeries = { rootId ->
                showDeleteConfirmDialog(rootId)
            }
        )

        rvTodos.layoutManager = LinearLayoutManager(this)
        rvTodos.adapter = adapter
        adapter.updateData(todoList)

        btnAdd.setOnClickListener {
            addNewTodo()
        }
    }

    override fun onStop() {
        super.onStop()
        storage.saveTodoList(todoList)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun addNewTodo() {
        val title = etNewTodo.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入任务标题", Toast.LENGTH_SHORT).show()
            return
        }

        val newItem = TodoItem(title = title)
        todoList.add(newItem)
        adapter.updateData(todoList)
        etNewTodo.text.clear()
        storage.saveTodoList(todoList)
        rvTodos.smoothScrollToPosition(adapter.itemCount - 1)
    }

    private fun updateItem(updated: TodoItem) {
        val index = todoList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            todoList[index] = updated
            adapter.updateData(todoList)
            storage.saveTodoList(todoList)
        }
    }

    private fun toggleCollapse(itemId: String) {
        val index = todoList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = todoList[index]
            todoList[index] = item.copy(isCollapsed = !item.isCollapsed)
            adapter.updateData(todoList)
            storage.saveTodoList(todoList)
        }
    }

    /**
     * 删除整个 Todo 系列（根任务 + 所有衍生子任务）
     */
    private fun deleteSeries(rootId: String) {
        val toDelete = mutableListOf<String>()
        // 找出所有以此 rootId 为根的任务（直接或间接 parentId 指向该系列）
        fun collectDescendantIds(id: String) {
            toDelete.add(id)
            val children = todoList.filter { it.parentId == id }
            for (child in children) {
                collectDescendantIds(child.id)
            }
        }
        collectDescendantIds(rootId)

        // 标记为已删除
        for (id in toDelete) {
            val idx = todoList.indexOfFirst { it.id == id }
            if (idx != -1) {
                todoList[idx] = todoList[idx].copy(isDeleted = true)
            }
        }

        // 保存到历史
        storage.saveHistory(todoList)
        // 保存活跃列表（自动过滤 isDeleted）
        storage.saveTodoList(todoList)
        // 从当前列表中移除已删除项
        todoList.removeAll { it.isDeleted }
        adapter.updateData(todoList)

        Toast.makeText(this, "已删除该任务系列，可在历史中查看", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmDialog(rootId: String) {
        val rootItem = todoList.find { it.id == rootId }
        val name = rootItem?.title ?: "该任务"
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除「$name」及其所有衍生子任务吗？\n删除后可在历史中查看。")
            .setPositiveButton("删除") { _, _ ->
                deleteSeries(rootId)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeriveDialog(parentItem: TodoItem) {
        val input = EditText(this)
        input.hint = "输入衍生任务标题..."

        AlertDialog.Builder(this)
            .setTitle("衍生新任务")
            .setMessage("从「${parentItem.title}」衍生出的新任务：")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val childTitle = input.text.toString().trim()
                if (childTitle.isEmpty()) {
                    Toast.makeText(this, "请输入任务标题", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val childItem = TodoItem(
                    title = childTitle,
                    parentId = parentItem.id
                )
                todoList.add(childItem)

                val updatedParent = parentItem.copy(childId = childItem.id)
                val parentIndex = todoList.indexOfFirst { it.id == parentItem.id }
                if (parentIndex != -1) {
                    todoList[parentIndex] = updatedParent
                }

                adapter.updateData(todoList)
                storage.saveTodoList(todoList)
                rvTodos.smoothScrollToPosition(adapter.itemCount - 1)

                Toast.makeText(
                    this,
                    "已建立关联：「${parentItem.title}」→「${childTitle}」",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}