package com.employee.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.employee.adapter.UserPostListAdapter.MyViewHolder
import com.employee.databinding.RowItemPostBinding
import com.employee.model.userPostModel.DataItem

class UserPostListAdapter(var context: Context?, private var allPostList: List<DataItem>) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = RowItemPostBinding.inflate(layoutInflater, viewGroup, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val dataItem = allPostList[i]
        myViewHolder.binding.apply {
            tvUserId.text = dataItem.userId.toString()
            tvTitle.text = dataItem.title
            tvBody.text = dataItem.body
        }
    }

    override fun getItemCount(): Int {
        return allPostList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class MyViewHolder(val binding: RowItemPostBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}