package com.bazzi.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bazzi.app.adapter.HistoryAdapter
import com.bazzi.app.model.TodoItem
import com.bazzi.app.util.TodoStorage
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var tvDateLabel: TextView
    private lateinit var llDatePicker: LinearLayout
    private lateinit var btnClearFilter: MaterialButton
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: HistoryAdapter
    private lateinit var storage: TodoStorage
    private var allHistory: List<TodoItem> = listOf()
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 标题点击返回
        findViewById<TextView>(R.id.tvTitle).setOnClickListener { finish() }

        storage = TodoStorage(this)

        tvDateLabel = findViewById(R.id.tvDateLabel)
        llDatePicker = findViewById(R.id.llDatePicker)
        btnClearFilter = findViewById(R.id.btnClearFilter)
        rvHistory = findViewById(R.id.rvHistory)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = HistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        // 加载历史
        allHistory = storage.loadHistory()
        updateList(allHistory)

        // 日期选择
        llDatePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    tvDateLabel.text = selectedDate
                    tvDateLabel.setTextColor(0xFF212121.toInt())
                    searchByDate(selectedDate!!)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // 清除筛选
        btnClearFilter.setOnClickListener {
            selectedDate = null
            tvDateLabel.text = "点击选择日期"
            tvDateLabel.setTextColor(0xFF9E9E9E.toInt())
            updateList(allHistory)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun searchByDate(dateStr: String) {
        val results = allHistory.filter { item ->
            val itemDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(item.createTime))
            itemDate == dateStr
        }
        updateList(results)
    }

    private fun updateList(items: List<TodoItem>) {
        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
            adapter.updateData(items)
        }
    }
}