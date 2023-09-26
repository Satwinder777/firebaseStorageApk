package com.example.myfirebaseprojectwithdb

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirebaseprojectwithdb.adapter.LoggedUserRcAdapter
import com.example.myfirebaseprojectwithdb.databinding.ActivityLoggedUserDetailsBinding
import com.example.myfirebaseprojectwithdb.myfireobj.auth
import com.example.myfirebaseprojectwithdb.myfireobj.db
import com.example.myfirebaseprojectwithdb.myfireobj.storageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

import android.database.Cursor

import android.provider.MediaStore
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.annotations.SerializedName

class LoggedUserDetails : AppCompatActivity(),LoggedUserRcAdapter.onItemClick {
    private lateinit var binding : ActivityLoggedUserDetailsBinding

    lateinit var  sharedPref :SharedPreferences
    lateinit var rc : RecyclerView
    lateinit var adapter : LoggedUserRcAdapter
    lateinit var mGoogleSignInClient: GoogleSignInClient

    var userList : MutableList<User> = mutableListOf()


    companion object{
        val TAG = "LoggedUserDetails"
        var createLiveButton:MutableLiveData<Boolean> = MutableLiveData()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoggedUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
         rc = binding.loggedUserRc
        sharedPref = getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        adapter = LoggedUserRcAdapter(null,this)
        GlobalScope.launch {
            launch(Dispatchers.IO){
                getAllUser()
            }
        }

        binding.logOutBtn.setOnClickListener {
            auth.signOut()
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra(navChain.LOGGED_USER_TO_MAIN.toString(),"")
            startActivity(intent)
            this.finish()
            sharedPref.edit().clear().apply()
            logoutOperation()
        }
        createLiveButton.observe(this, Observer {
            if (it){
                binding.createuser.visibility = View.VISIBLE
            }else{
                binding.createuser.visibility = View.GONE
            }
        })
        binding.createuser.setOnClickListener {
            startActivity(Intent(this,CustomSignUpActivity::class.java))

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun getAllUser(){
        val listUser:MutableList<User> = mutableListOf()
//       val snapshot = db.collection("users")
//            .get().await()

        val snapshot = db.collection("users")
            .get().await()


        snapshot.documents.forEach {
                        if (it.contains("img") || it.contains("firstName") || it.contains("lastName") || it.contains("password") || it.contains("userId")){
                            var img = it.get("img")
                            var firstName = it.get("firstName")
                            var lastName = it.get("lastName")
                            var email = it.get("email")
                            var pd = it.get("password")
                            var ui = it.id
                            Log.e(TAG+50, "getAllUser:$firstName\n$img\n$lastName\n$pd\n$ui\n=========", )
                            val addUserInstance = User(
                                img as String,
                                firstName as String,
                                lastName as String,
                                email as String,
                                pd as String,
                                ui as String,
                                )
                            listUser.add(addUserInstance)

                        }else{
                            Toast.makeText(this, "item does not contains ", Toast.LENGTH_SHORT).show()
                        }
                        Log.e(TAG+45, "getAllUser: ${it}", )
                    }  //for each end





//        for ( data in snapshot.documents){
//
//            try {
//
//
////                var  userdataObj = data.toObject(User::class.java)
////                Log.e("userdataObj", "getAllUser: $userdataObj", )
//                val first = data.get("firstName")
//                val last = data.get("lastName")
//                var uri = data.get("img")
//                val password = data.get("password")
//                val email = data.get("email")
//                val userId = data.id
//
//                Log.e("FileName123", "getAllUser: ${Uri.parse(uri as String).toFile().name},,size>>${snapshot.documents.size}", )
////                try {
////                     storageRef.child("images/${Uri.parse(uri as String).toFile().name}").getFile(
////                        Uri.parse(uri))
////                        .addOnCompleteListener{
////
////
////                            Log.d("DownloadImg5", "getAllUser: ")
////                        }
////                        .addOnFailureListener{
////                            Log.e("DownloadImg", "getAllUser: ${ it}", )
////                        }
////                }
////                catch (e:Exception){
////                    Log.e("currentImg12", " exp >>getAllUser:>> $e", )
////                }
//
//                var user = User(uri as String,first as String,last as String,email as String,password as String,userId)
//                Log.d(TAG+32, "getAllUser:11 $first$last\n$uri\n$password\n${email.toUri()} \n========================")
//                listUser.add(user)
////                        val user = data.toObject(User::class.java)
////                        if (user != null) {
////                            listUser.add(user)
////                        }
//
//                userList.add(user)
//
//            }
//            catch (e:Exception){
//                Log.d("exceptionUser", "getAllUser: ${e}")//@gmail.com
//            }
//
//        }
        GlobalScope.launch {
            try {
                if (listUser.isNullOrEmpty().not()){
                    var adapter = LoggedUserRcAdapter(listUser,this@LoggedUserDetails)
                    launch(Dispatchers.Main){
                        rc.adapter = adapter
                    }
                    adapter.notifyDataSetChanged()
                    Log.e(TAG+21, "getAllUser: if block data>>$listUser")
                }else{
                    Log.e(TAG+21, "getAllUser: else data >>$listUser", )
                }
            }
            catch (e:Exception){
                Log.e(TAG, "getAllUser: $e", )
            }
          
        }

if (listUser.size<=3){
    createLiveButton.postValue(true)
}else{
    createLiveButton.postValue(false)
}

        loadImagesFromStorage()
    }

    override fun update(data: User) {

        var intent = Intent(this,CustomSignUpActivity::class.java)
        intent.putExtra(naviDirect.LOGGEDLIST_TO_UPDATE.toString(),naviDirect.LOGGEDLIST_TO_UPDATE.toString())
        intent.putExtra(userCodes.FIRST_NAME.toString(),data.firstName)
        intent.putExtra(userCodes.LAST_NAME.toString(),data.lastName)
        intent.putExtra(userCodes.EMAIL.toString(),data.email)
        intent.putExtra(userCodes.PASSWORD.toString(),data.password)
        intent.putExtra(userCodes.PROFILE_IMG.toString(),data.img)
        intent.putExtra(userCodes.User_Id.toString(),data.userid)
        startActivity(intent)
    }

    override fun deleteUser(data: User) {
        deleteUser1(data)
    }

    override fun itemCliked(user: User) {
        try {
            val localFile = user.img?.toUri()?.toFile()//File.createTempFile("images", ".jpg")
            Log.e("DownLoadListner", "getAllUser: ${localFile?.name}")

//            Log.d("getPath", "itemCliked: $path")
            if (localFile != null) {
                storageRef.child("images/${user.img!!.toUri().toFile().name}").getFile(localFile)
                    .addOnCompleteListener{
                        Log.e("DownLoadListner", "getAllUser: ${it} downloadded")
                    }
                    .addOnFailureListener {
                        Log.e("DownLoadListner", "getAllUser: $it")
                    }
            }
        }
        catch (e:Exception){
            Log.e("DownLoadListner", "itemCliked: $e", )
        }


    }


    fun deleteUser1(user: User){

        myfireobj.db.collection("users").document(user.userid.toString())
            .delete()
            .addOnCompleteListener {
                Toast.makeText(this, "successfully deleted user", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,LoggedUserDetails::class.java))
                deleteFromStorage(user)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

        fun deleteFromStorage(user: User){
                var fileRef = storageRef.child("images/${user.email}")
                    .delete()
                    .addOnCompleteListener {
                        Log.d("FileDeleted", "deleteFromStorage: ${it}>>successfully deleted fileref${user.img?.toUri()?.toFile()?.name}")
                    }
                    .addOnFailureListener {
                        Log.e("FileDeleted", "deleteFromStorage: $it", )
                    }
        }

private suspend fun loadImagesFromStorage(){
    var imagesList = mutableListOf<Uri>()
    var listStoragePreferences = storageRef.child("images").listAll().await()
    for ( item in listStoragePreferences.items){
        Log.e("FileName12", "loadImagesFromStorage:${item.name} ", )
        try {
            val downloadUrl = item.downloadUrl.await()
            val uri = Uri.parse(downloadUrl.toString())
            imagesList.add(uri)
//            userList.forEach {
//                var name = Uri.parse(it.img).toFile().name
//                if (name==item.name){
//                    adapter.setImg(uri,userList.indexOf(it))
//                    Log.e(TAG, "loadImagesFromStorage: ${userList.indexOf(it)} matched data")
//                }
//            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur while fetching download URLs
            Log.e("imagesList", "getAllUser: $e", )
        }
      
    }//file:///data/user/0/com.example.myfirebaseprojectwithdb/cache/Wed%20Sep%2020%2015%3A58%3A20%20GMT%2B05%3A30%202023
    
   
    Log.d("imagesList", "getAllUser: $imagesList")
}
   @SuppressLint("SuspiciousIndentation")
   private fun logoutOperation(){
       val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           .requestIdToken(getString(R.string.default_web_client_id))
           .requestEmail()
           .build()

     mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
       mGoogleSignInClient.signOut()
    }

}
enum class userCodes{
    FIRST_NAME,
    LAST_NAME,
    PROFILE_IMG,
    EMAIL,
    PASSWORD,
    User_Id,
}
enum class naviDirect{
    LOGGEDLIST_TO_UPDATE,
    SIGNUP_SCREEN
}


data class User(
    @SerializedName("img")
    var img: String?,

    @SerializedName("firstName")
    val firstName:String,

    @SerializedName("lastName")
    val lastName:String,

    @SerializedName("email")
    val email:String,

    @SerializedName("password")
    val password:String,

    @SerializedName("userid")
    val userid:String?=null
)


