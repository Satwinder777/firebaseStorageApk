package com.example.myfirebaseprojectwithdb

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.UriCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.myfirebaseprojectwithdb.databinding.ActivityMainBinding
import com.example.myfirebaseprojectwithdb.myfireobj.storageRef
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.google.firebase.FirebaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {
    val tag = "testcase1"
    private lateinit var binding : ActivityMainBinding
    private lateinit var sharedPref : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    var googleProfileUri:Uri?=null



//@gmail.com
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    sharedPref = getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
    editor= sharedPref.edit()
    isUserLogged()
    handleIntentData()
    initgoogleLogin()


        binding.signUpId.setOnClickListener {
               startActivity(Intent(this,CustomSignUpActivity::class.java))
            this.finish()
        }
    binding.SignInUsingGoogle.setOnClickListener {
        loginUsingIntent()
    }
    }

    private fun login(email:String , password:String){
        try{
        myfireobj.auth.signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener {
                if(it.isSuccessful){

                    startActivity(Intent(this,LoggedUserDetails::class.java))
                    Log.d(tag, "login: successfully logged ${it.result.user?.displayName}${it.result.user?.email}")
                    editor.putString(phref.PHREF_KEY.toString(),it.result.user?.email)
                    editor.apply()
                    this.finish()
                }
            }
            .addOnFailureListener {
                Log.d(tag, "error ${it.message},$it")
                when(it){
                    is FirebaseException ->{
                        Toast.makeText(this, "INVALID_LOGIN_CREDENTIALS", Toast.LENGTH_SHORT).show()
                    }
                    else->{
                        Log.d(tag+1, "login: $it")}
                }
            }
        }
        catch (e:Exception){
            Log.e(tag, "onCreate: loginbtn click  ${e.message}", )
        }
    }

    private fun isUserLogged(){
        if (sharedPref.getString(phref.PHREF_KEY.toString(),"").isNullOrEmpty()){  //Satwinder@192939
           binding.loginBtn.setOnClickListener {
               login(binding.mailId.text.toString(),binding.password.text.toString())
           }
            Log.e("checkuser", "isUserLogged: if block ${sharedPref.getString(phref.PHREF_KEY.toString(),"")} ", )
        }
        else{
            Log.e("checkuser", "isUserLogged: else block block ${sharedPref.getString(phref.PHREF_KEY.toString(),"")} ", )

            startActivity(Intent(this,LoggedUserDetails::class.java))
           this.finish()
       }
    }

  fun initgoogleLogin() {
      val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestIdToken(getString(R.string.default_web_client_id))
          .requestEmail()
          .build()

      mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

  }

    private fun loginUsingIntent(){
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            RC_SIGN_IN->{
               var task = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    var account = task.getResult(ApiException::class.java)
                    handleLogin(account)
                }
                catch (e:Exception){
                    Log.w("SignInFailed", "Google sign-in failed", e)
                    Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show()
                }
            }
            else->{
                Log.e("CodeTEst", "onActivityResult: $requestCode", )
            }
        }




    }
    @SuppressLint("SuspiciousIndentation")
    private fun handleLogin(account: GoogleSignInAccount) {
        if (account!=null){

            var name = account.displayName
            var img = account.photoUrl
            var  email = account.email
            var idToken = account.idToken
//            var name1 = account.account?.name

            googleProfileUri = img
            Log.d("UserDAta123", "handleLogin: ${name}>> jjk>> ${img}>> ${email}>> ${idToken} ")
            editor.putString(phref.PHREF_KEY.toString(),email)
            editor.apply()
            val freshUser = User(img.toString(),
                name?.split(" ")?.get(0).toString(),
                name?.split(" ")?.get(1).toString(),
                email.toString(),
                password = "123456",
                ""

            )

                savedata(freshUser)

        }

    }
    @SuppressLint("SuspiciousIndentation")
    fun savedata(user: User){
        var date = Date()

      var store =  myfireobj.db.collection("users").document().set(user)
          .addOnCompleteListener {

                Toast.makeText(this, "logged", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,LoggedUserDetails::class.java))
                Log.e("inputstreamtest", "savedata: url>> ${user.img }", )

                GlobalScope.launch (Dispatchers.IO) {

                    var inputstream = getInputStreamFromImageUrl(user.img.toString())

                            if (inputstream!=null){
                                storageRef.child("images/${user.email}")
                                    .putStream(inputstream)
                                    .addOnCompleteListener{
                                        Toast.makeText(this@MainActivity, "image uploaded!!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@MainActivity, "error occured !! ", Toast.LENGTH_SHORT).show()
                                        Log.e("inputstreamtest", "savedata: $it", )
                                    }

                            }

                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                Log.e("errorData", "savedata: $it", )
            }

    }
    @SuppressLint("SuspiciousIndentation")
    private fun saveBitmapAsFile(file: File) {
        val file = File(getExternalFilesDir(null), file.name)

        try {
            var fileRef =  storageRef.child("images/user_Profile.jpg")


           var fileUri =  Uri.fromFile(file)


                fileRef.putFile(fileUri)
                    .addOnSuccessListener{
                        //                                Log.d("FileutilsDAta", "savedata: addOnCompleteListener ${it.result.totalByteCount} ")
                        //                            saveImgStorage(Uri.parse(user.img))
                        Toast.makeText(this , "image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Log.d("FileutilsDAta", "savedata:error while image uploading >>$fileUri>> $it")
                        Toast.makeText(this, "error while image uploading", Toast.LENGTH_SHORT).show()

                    }


        }catch (e:Exception){
            Log.e("FileutilsDAta12", "savedata: exp >>$e", )
        }

//        try {
//            val stream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) // Change format and quality as needed
//            stream.flush()
//            stream.close()
//            Log.e("testfileName12", "saveBitmapAsFile:${file.name} 11 ", )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("testfileName12", "saveBitmapAsFile:${file.name} $e 222 ", )
//
//        }

    }
    private fun saveImgStorage(uri: Uri){
        storageRef.child("images/").putFile(uri)
            .addOnCompleteListener {
                Log.e("saveImagetoStorage", "saveImgStorage: exp>>${it.result.metadata} ", )
            }
            .addOnFailureListener {
                Log.e("saveImagetoStorage", "saveImgStorage: exp>>$it ", )
            }
    }
    private fun handleIntentData(){
        var data = intent.getStringExtra(navChain.LOGGED_USER_TO_MAIN.toString())
        if (data==navChain.LOGGED_USER_TO_MAIN.toString()){
            editor.clear().apply()

            Log.e("dataHandle", "handleIntent: if call $data", )
        }else{
            Log.e("dataHandle", "handleIntent: if call $data", )
        }
    }
    fun getInputStreamFromImageUrl(imageUrl: String): InputStream? {
        val url = URL(imageUrl)

        val inputStream: InputStream? = try {
            Log.e("inputstreamtest", "getInputStreamFromImageUrl: $url", )
            url.openStream()
        } catch (e: IOException) {
            // Handle the error
            null
        }

        if (inputStream == null) {
            // The image could not be found or downloaded.
            return null
        }

        return inputStream
    }

}
enum class navChain{
    LOGGED_USER_TO_MAIN
}
enum class phref{
    PHREF_KEY
}