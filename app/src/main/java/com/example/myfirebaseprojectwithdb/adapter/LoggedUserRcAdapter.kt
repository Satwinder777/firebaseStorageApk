package com.example.myfirebaseprojectwithdb.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myfirebaseprojectwithdb.R
import com.example.myfirebaseprojectwithdb.User
import com.google.android.material.button.MaterialButton

class LoggedUserRcAdapter(var listUser:MutableList<User>?,val onclickItem:onItemClick): RecyclerView.Adapter<LoggedUserRcAdapter.MyListClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListClass {
        var view =  LayoutInflater.from(parent.context).inflate(R.layout.user_item_rc,parent,false)
        return MyListClass(view)
    }

    override fun getItemCount(): Int {
        return listUser?.size?:0
    }

    fun addUsers(list: MutableList<User>){
        listUser?.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyListClass, position: Int) {
        holder.apply {
            listUser?.get(position)?.let { bind(it) }
            if (listUser!=null){
                Log.d("userListIs", "onBindViewHolder: $listUser")
            }
            else{
                Log.d("userListIs", "onBindViewHolder: null call $listUser")
            }
            holder.updatebtn.setOnClickListener {
                listUser?.get(position)?.let { it1 -> onclickItem.update(it1) }
            }
            holder.deletebtn.setOnClickListener {
                listUser?.get(position)?.let { it1 -> onclickItem.deleteUser(it1) }
            }
            holder.apply {
//                itemView.setOnClickListener {
//                    listUser?.get(position)?.let { it1 -> onclickItem.itemCliked(it1) }
//                }
            }
        }
    }
    class MyListClass(view: View): RecyclerView.ViewHolder(view) {
        var profileImage = view.findViewById<ImageView>(R.id.profileItem)
        var name = view.findViewById<TextView>(R.id.userName)
        var email = view.findViewById<TextView>(R.id.userEmail)
        var password = view.findViewById<TextView>(R.id.userPassword)
        var updatebtn = view.findViewById<MaterialButton>(R.id.updatebtn)
        var deletebtn = view.findViewById<MaterialButton>(R.id.deletebtn)
        @SuppressLint("SetTextI18n")
        fun bind(item : User){
//            profileImage.setImageURI(item.img)

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image while loading (optional)

            // Load the image using Glide
            Glide.with(itemView.context)
                .load(Uri.parse(item.img))
                .apply(requestOptions)
                .into(profileImage)
//            profileImage.setImageURI(Uri.parse(item.img))
            name.setText("${item.firstName } ${item.lastName }")
            email.setText(item.email)
            password.setText(item.password)
        }
    }

    fun setData(data:MutableList<User>){
        listUser?.addAll(data)
        notifyDataSetChanged()
    }
    fun setImg(uri: Uri,position: Int){
        listUser?.get(position)?.img = uri.toString()

        notifyDataSetChanged()
    }


    interface onItemClick{
        fun update(data:User)
        fun deleteUser(data: User)
        fun itemCliked(user: User)
    }
}
