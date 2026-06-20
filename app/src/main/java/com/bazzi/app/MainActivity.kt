package com.bazzi.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bazzi.app.adapter.FunctionAdapter
import com.bazzi.app.model.FunctionItem

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvFunctions = findViewById<RecyclerView>(R.id.rvFunctions)
        rvFunctions.layoutManager = LinearLayoutManager(this)

        val functions = listOf(
            FunctionItem(
                name = "TodoList",
                description = "关联任务的待办事项列表",
                targetActivity = TodoListActivity::class.java
            )
        )

        rvFunctions.adapter = FunctionAdapter(functions)
    }
}