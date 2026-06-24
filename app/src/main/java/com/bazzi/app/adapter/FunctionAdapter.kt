package com.bazzi.app.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bazzi.app.R
import com.bazzi.app.model.FunctionItem

class FunctionAdapter(
    private val functions: List<FunctionItem>
) : RecyclerView.Adapter<FunctionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_function, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = functions[position]
        holder.bind(item)
    }

    override fun getItemCount() = functions.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFunctionName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvFunctionDesc)

        fun bind(item: FunctionItem) {
            tvName.text = item.name
            tvDesc.text = item.description
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, item.targetActivity)
                itemView.context.startActivity(intent)
            }
        }
    }
}