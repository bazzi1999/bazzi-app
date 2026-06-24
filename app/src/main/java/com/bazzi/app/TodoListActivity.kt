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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TodoListActivity : AppCompatActivity() {

    private val todoList = mutableListOf<TodoItem>()
    private lateinit var adapter: TodoAdapter
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var rvTodos: RecyclerView
    private lateinit var storage: TodoStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        // 初始化存储
        storage = TodoStorage(this)

        btnAdd = findViewById(R.id.btnAdd)
        rvTodos = findViewById(R.id.rvTodos)

        // 信箱图标 - 历史
        findViewById<android.widget.ImageView>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

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
            },
            onEditItem = { item ->
                showEditDialog(item)
            }
        )

        rvTodos.layoutManager = LinearLayoutManager(this)
        rvTodos.adapter = adapter
        adapter.updateData(todoList)

        // FAB 加号按钮点击 -> 弹窗添加任务
        btnAdd.setOnClickListener {
            showAddTodoDialog()
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

    /**
     * 弹窗添加新任务（标题+描述）
     */
    private fun showAddTodoDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_add_todo, null)
        val etTitle = layout.findViewById<EditText>(R.id.etAddTitle)
        val etDescription = layout.findViewById<EditText>(R.id.etAddDescription)

        AlertDialog.Builder(this)
            .setTitle("添加新任务")
            .setView(layout)
            .setPositiveButton("添加") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(this, "请输入任务标题", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val description = etDescription.text.toString().trim()
                val newItem = TodoItem(title = title, description = description)
                todoList.add(newItem)
                adapter.updateData(todoList)
                storage.saveTodoList(todoList)
                rvTodos.smoothScrollToPosition(adapter.itemCount - 1)
            }
            .setNegativeButton("取消", null)
            .show()
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
     * 弹出编辑对话框，修改 Todo 的标题和描述
     */
    private fun showEditDialog(item: TodoItem) {
        val layout = layoutInflater.inflate(R.layout.dialog_edit_todo, null)
        val etTitle = layout.findViewById<EditText>(R.id.etEditTitle)
        val etDescription = layout.findViewById<EditText>(R.id.etEditDescription)

        etTitle.setText(item.title)
        etDescription.setText(item.description)

        AlertDialog.Builder(this)
            .setTitle("编辑任务")
            .setView(layout)
            .setPositiveButton("保存") { _, _ ->
                val newTitle = etTitle.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val newDescription = etDescription.text.toString().trim()
                val updated = item.copy(title = newTitle, description = newDescription)
                updateItem(updated)
                Toast.makeText(this, "已修改", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
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

    /**
     * 弹窗衍生新任务，同时输入标题和描述
     */
    private fun showDeriveDialog(parentItem: TodoItem) {
        val layout = layoutInflater.inflate(R.layout.dialog_add_todo, null)
        val etTitle = layout.findViewById<EditText>(R.id.etAddTitle)
        val etDescription = layout.findViewById<EditText>(R.id.etAddDescription)
        etTitle.hint = "输入衍生任务标题..."
        etDescription.hint = "衍生任务描述（选填）..."

        AlertDialog.Builder(this)
            .setTitle("衍生新任务")
            .setMessage("从「${parentItem.title}」衍生：")
            .setView(layout)
            .setPositiveButton("确定") { _, _ ->
                val childTitle = etTitle.text.toString().trim()
                if (childTitle.isEmpty()) {
                    Toast.makeText(this, "请输入任务标题", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val childDescription = etDescription.text.toString().trim()

                val childItem = TodoItem(
                    title = childTitle,
                    description = childDescription,
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