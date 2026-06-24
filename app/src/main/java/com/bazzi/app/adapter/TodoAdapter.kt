package com.bazzi.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bazzi.app.R
import com.bazzi.app.model.TodoItem
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private var items: MutableList<TodoItem>,
    private val onToggleCompleted: (TodoItem) -> Unit,
    private val onDeriveTask: (TodoItem) -> Unit,
    private val onToggleCollapse: (String) -> Unit,
    private val onDeleteSeries: (String) -> Unit,
    private val onEditItem: (TodoItem) -> Unit  // 新增：点击编辑回调
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    fun updateData(newItems: MutableList<TodoItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun getVisibleItems(): List<TodoItem> {
        return items.filter { item ->
            if (item.parentId == null) {
                true
            } else {
                var current = item
                var visible = true
                while (current.parentId != null) {
                    val parent = items.find { it.id == current.parentId }
                    if (parent == null) break
                    if (parent.isCollapsed) {
                        visible = false
                        break
                    }
                    current = parent
                }
                visible
            }
        }
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
        private val cbCompleted: CheckBox = itemView.findViewById(R.id.cbCompleted)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnDerive: View = itemView.findViewById(R.id.btnDerive)
        private val btnDelete: View = itemView.findViewById(R.id.btnDelete)

        // 缓存当前绑定的 item，用于点击回调
        private var currentItem: TodoItem? = null

        init {
            // 点击整行内容区进行编辑
            itemView.setOnClickListener {
                currentItem?.let { item ->
                    onEditItem(item)
                }
            }

            // 只在初始化时设置一次 CheckBox 点击事件
            cbCompleted.setOnClickListener {
                currentItem?.let { item ->
                    val updated = item.copy(isCompleted = !item.isCompleted)
                    onToggleCompleted(updated)
                }
            }

            // 折叠箭头点击
            ivCollapse.setOnClickListener {
                currentItem?.let { item ->
                    onToggleCollapse(item.id)
                }
            }

            // 衍生按钮点击
            btnDerive.setOnClickListener {
                currentItem?.let { item ->
                    onDeriveTask(item)
                }
            }

            // 删除按钮点击
            btnDelete.setOnClickListener {
                currentItem?.let { item ->
                    onDeleteSeries(item.id)
                }
            }
        }

        fun bind(item: TodoItem) {
            currentItem = item

            // 标题和完成状态
            tvTitle.text = item.title
            tvTitle.paint.isStrikeThruText = item.isCompleted

            // 描述
            if (item.description.isNotEmpty()) {
                tvDescription.text = item.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // 设置 CheckBox 状态（不触发监听器）
            cbCompleted.isChecked = item.isCompleted

            // 日期显示
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

            // 折叠功能：只有根任务且子任务时显示箭头
            val isRootWithChildren = item.parentId == null && items.any { it.parentId == item.id }
            ivCollapse.visibility = if (isRootWithChildren) View.VISIBLE else View.GONE
            if (isRootWithChildren) {
                ivCollapse.rotation = if (item.isCollapsed) 0f else 180f
            }

            // 左侧缩进（只缩进左侧）
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

            // 衍生新任务按钮（任何任务都可衍生，只要还没有子任务）
            btnDerive.visibility = if (item.childId == null) View.VISIBLE else View.GONE

            // 删除按钮（仅根任务）
            btnDelete.visibility = if (item.parentId == null) View.VISIBLE else View.GONE
        }
    }
}