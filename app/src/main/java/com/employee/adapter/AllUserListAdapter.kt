package com.employee.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.employee.adapter.AllUserListAdapter.MyViewHolder
import com.employee.databinding.RowItemUserBinding
import com.employee.room.ListRoom
import com.employee.ui.fragment.UsersFragment

class AllUserListAdapter(var context: Context?, private var allUserList: List<ListRoom>, private val activity: UsersFragment) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = RowItemUserBinding.inflate(layoutInflater, viewGroup, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val dataItem = allUserList[i]

        myViewHolder.binding.apply {
            tvName.text = dataItem.name
            tvEmail.text = dataItem.email
            tvGender.text = dataItem.gender

            /*Button Delete OnCLick Method*/
            btnDelete.setOnClickListener {
                activity.deleteRecord(dataItem.email!!,dataItem.id)
            }

            /*Select Particular user Onclick Method*/
            rowItem.setOnClickListener{
                activity.selectUser(dataItem.id,dataItem.name,dataItem.gender,dataItem.email,dataItem.status,dataItem.createdAt,dataItem.updatedAt)
            }
        }
    }

    override fun getItemCount(): Int {
        return allUserList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class MyViewHolder(val binding: RowItemUserBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}