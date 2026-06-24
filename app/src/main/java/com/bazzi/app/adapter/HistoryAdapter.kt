package com.bazzi.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bazzi.app.R
import com.bazzi.app.model.TodoItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onDeleteHistoryItem: (TodoItem) -> Unit = {}
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var allItems: List<TodoItem> = listOf()
    private val expandedRootIds = mutableSetOf<String>()

    fun updateData(newItems: List<TodoItem>) {
        allItems = newItems
        notifyDataSetChanged()
    }

    /**
     * 获取可见列表：默认只显示根任务，展开的根任务显示其子任务
     */
    private fun getVisibleItems(): List<TodoItem> {
        val result = mutableListOf<TodoItem>()
        for (item in allItems) {
            if (item.parentId == null) {
                // 根任务始终显示
                result.add(item)
                // 如果根任务已展开，添加所有子任务（保留层级缩进）
                if (item.id in expandedRootIds) {
                    result.addAll(getAllDescendants(item.id))
                }
            }
            // 非根任务不单独添加，由上面的递归添加
        }
        return result
    }

    private fun getAllDescendants(parentId: String): List<TodoItem> {
        val result = mutableListOf<TodoItem>()
        val children = allItems.filter { it.parentId == parentId }
        for (child in children) {
            result.add(child)
            result.addAll(getAllDescendants(child.id))
        }
        return result
    }

    override fun getItemCount() = getVisibleItems().size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getVisibleItems()[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCollapse: ImageView = itemView.findViewById(R.id.ivCollapse)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(item: TodoItem) {
            tvTitle.text = item.title
            // 历史记录不显示删除线
            tvTitle.paint.isStrikeThruText = false

            // 描述
            if (item.description.isNotEmpty()) {
                tvDescription.text = item.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // 日期
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            cal.timeInMillis = item.createTime
            val itemYear = cal.get(Calendar.YEAR)
            val dateFormat = if (itemYear == currentYear) {
                SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            }
            tvDate.text = dateFormat.format(Date(item.createTime))

            // 隐藏操作按钮
            itemView.findViewById<View>(R.id.btnDerive).visibility = View.GONE
            itemView.findViewById<View>(R.id.btnDelete).visibility = View.GONE
            itemView.findViewById<View>(R.id.cbCompleted).visibility = View.GONE

            // 折叠：根任务且有子任务时显示箭头
            val hasChildren = allItems.any { it.parentId == item.id }
            if (item.parentId == null && hasChildren) {
                ivCollapse.visibility = View.VISIBLE
                ivCollapse.rotation = if (item.id in expandedRootIds) 180f else 0f
                itemView.setOnClickListener {
                    if (item.id in expandedRootIds) {
                        expandedRootIds.remove(item.id)
                    } else {
                        expandedRootIds.add(item.id)
                    }
                    notifyDataSetChanged()
                }
            } else {
                ivCollapse.visibility = View.GONE
                itemView.setOnClickListener(null)
            }

            // 仅根任务可长按删除整个系列
            if (item.parentId == null) {
                itemView.setOnLongClickListener {
                    onDeleteHistoryItem(item)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
            }

            // 左侧缩进（与主界面一致）
            val indent = if (item.parentId != null) {
                itemView.resources.getDimensionPixelSize(R.dimen.indent_padding)
            } else {
                0
            }
            (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                params.leftMargin = indent
                params.rightMargin = 0
                itemView.layoutParams = params
            }
        }
    }
}