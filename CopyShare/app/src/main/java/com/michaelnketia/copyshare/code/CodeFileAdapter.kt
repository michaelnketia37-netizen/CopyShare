package com.michaelnketia.copyshare.code

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaelnketia.copyshare.databinding.ItemCodeFileBinding

class CodeFileAdapter(
    private val list: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CodeFileAdapter.Holder>() {

    inner class Holder(val binding: ItemCodeFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val binding = ItemCodeFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return Holder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val file = list[position]

        holder.binding.txtFile.text = file

        holder.itemView.setOnClickListener {
            onClick(file)
        }
    }
}