package com.example.myfirebaseprojectwithdb

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import androidx.core.net.toUri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.FileUtils
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myfirebaseprojectwithdb.databinding.ActivityCustomSignUpBinding
import com.example.myfirebaseprojectwithdb.myfireobj.auth
import com.example.myfirebaseprojectwithdb.myfireobj.storageRef
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.*
import kotlin.random.Random

class CustomSignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCustomSignUpBinding
    var userID:String? = null
    var direction:String ?=null
    var user:User?=null
    companion object{
        val TAG = "SATWINDERQWERASDF"
        const val PICK_IMAGE_REQUEST_CODE = 2001
//        private val PERMISSION_OPENGALLERY: Array<String> =
//            arrayOf("android.permission.READ_EXTERNAL_STORAGE")
        var profileUri:Uri ?= null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
       direction = intent.getStringExtra(naviDirect.LOGGEDLIST_TO_UPDATE.toString())
        try {
            handleUserNavigation()
        }
        catch (e:Exception){
            Log.e(TAG, "onCreate: $e", )
        }

        binding.profileImg.setOnClickListener {
//
//            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED){
//                openGallery()
//            }else{
//                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
//            }





            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Code for devices running Android 5.0 (Lollipop) or higher
                if (ContextCompat.checkSelfPermission(this,  android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    // Replace PERMISSION_OPENGALLERY with the actual permission(s) you want to request.
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                val permissionsToRequest = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,storagePermission)
                    this.requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), PICK_IMAGE_REQUEST_CODE)
//                ActivityCompat.requestPermissions(this, arrayOf(storagePermission,android.Manifest.permission.READ_EXTERNAL_STORAGE), PICK_IMAGE_REQUEST_CODE)
                }
            } else {
                // Code for devices running Android versions earlier than 4.1
                if (ContextCompat.checkSelfPermission(this,  android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    // Replace PERMISSION_OPENGALLERY with the actual permission(s) you want to request.
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                val permissionsToRequest = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,storagePermission)
                    this.requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PICK_IMAGE_REQUEST_CODE)
//                ActivityCompat.requestPermissions(this, arrayOf(storagePermission,android.Manifest.permission.READ_EXTERNAL_STORAGE), PICK_IMAGE_REQUEST_CODE)
                }
            }



        }
        binding.SignUp.setOnClickListener {

            if (direction != naviDirect.LOGGEDLIST_TO_UPDATE.toString()){
                profileUri?.let { it1 -> checkValidation(
                    binding.profileImg
                    ,binding.firstnameid
                    ,binding.lastnameid
                    ,binding.emailSignup
                    ,binding.passwordId
                    ,binding.confirm
                )
            }

            }
            else{
                user?.let { it1 -> updateUser(it1) }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

       when(requestCode){
           PICK_IMAGE_REQUEST_CODE->{
               if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   openGallery()
                   Log.d("coderTam", "onRequestPermissionsResult:$ permission  granted ")

               }else{
                   Log.d("coderTam", "onRequestPermissionsResult:$ permission not granted ")
//                   val permissionsToRequest = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                   this.requestPermissions(permissionsToRequest, PICK_IMAGE_REQUEST_CODE)
               }
           }
           else->{
               Log.d("tag1234", "onRequestPermissionsResult: $requestCode")
           }
       }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        when(requestCode){
            PICK_IMAGE_REQUEST_CODE->{

                Log.e(TAG, "onActivityResult: $requestCode", )


                data?.data?.let {
                    var file = uriToFile(this, it)
                    Log.d("MyFile", "onActivityResult: ${file}")
                   var uri = Uri.fromFile(file)
                    Log.d("MyFile", "onActivityResult: ${uri}")
                    profileUri = uri
                }
                Glide.with(this)
                    .load(profileUri)
                    .into(binding.profileImg)

            }
            else->{
                Log.d(TAG, "onActivityResult: $requestCode ")
            }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun signUp(email:String,password:String){

        myfireobj.auth.createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d(TAG, "signUp: ${it.result.user?.email}>>${it.result.user?.displayName}")
                }

                try {
                    storageRef.child("images/${email}").putFile(profileUri as Uri)
                        .addOnCompleteListener{
                            Log.d("fileupdate", "signUp: file uploaded")
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                        }
                }
                catch (e:Exception){
                    Log.e(TAG, "signUp: $e", )
                }
              
            }
            .addOnFailureListener {
                Log.d(TAG, "signUp: $it")
            }

    }

    private fun checkValidation(image: ImageView?, firstname :EditText?,lastName :EditText?,email: EditText?,password: EditText?,confirmPassword:EditText){

        val list = listOf(image,firstname,lastName,email,password,confirmPassword)
        var shouldBreak = false
        val apassword = list.get(4) as EditText
        val cpassword = list.get(5) as EditText
        list.forEach {
             when(it){
                is ImageView->{
                    if (it.drawable==null){
                        shouldBreak = true
                        Toast.makeText(this, "please set profile Image", Toast.LENGTH_SHORT).show()
                    }
                }
                is  EditText->{
                    if (it.text.isEmpty()){
                        shouldBreak = true
                        it.setError("Empty data found !!,",ContextCompat.getDrawable(this,R.drawable.error_ic))



                    }
                    if (it == binding.emailSignup){
                        shouldBreak = it.text.toString().isEmailVerified().not()
                        if (shouldBreak){
                            binding.emailSignup.setError("format Mismatch !!,",ContextCompat.getDrawable(this,R.drawable.error_ic))
                        }
                    }

                }
                 else->{
                     Log.d(TAG, "checkValidation: $it")}
            }
            if (shouldBreak) {
                Log.d(TAG, "checkValidation: $it $shouldBreak")
                return@forEach
            }
        }
        if (shouldBreak==false && apassword.text.toString()==cpassword.text.toString()){
            signUp(email!!.text.trim().toString(), password!!.text.trim().toString())
            profileUri?.normalizeScheme()?.let {
                storetoDb(
                    it.toString(), firstname!!.text.trim().toString(),
                    lastName!!.text.trim().toString(),
                    email.text.trim().toString(),
                    password.text.trim().toString(),
                    confirmPassword.text.trim().toString())//@gmail.com
               // jhfkwadh

            }

        }else{
            Toast.makeText(this, "something went wrong ", Toast.LENGTH_SHORT).show()
           binding.confirm.setError("pasword mismatch ",ContextCompat.getDrawable(this,R.drawable.error_ic))

        }

    }
    private fun storetoDb(image: String, firstname :String,lastName :String,email: String,password: String,confirmPassword:String){

        var user = User(image, firstname, lastName, email, password)
        myfireobj.db.collection("users")
            .add(user).addOnCompleteListener {
               var id = it.result.id
                Log.d(TAG, "storetoDb: id >>$id")
                openIntent()
            }
            .addOnFailureListener {
                Log.d(TAG, "storetoDb: $it")
            }
    }
    private fun openIntent(){
        startActivity(Intent(this,LoggedUserDetails::class.java))
    }

    private fun handleUserNavigation(){
        //naviDirect

        if (direction == naviDirect.LOGGEDLIST_TO_UPDATE.toString()){
            Log.d(TAG, "handleUserNavigation: if block signUpscreen $direction ")
            var first = intent.getStringExtra(userCodes.FIRST_NAME.toString())
            var last = intent.getStringExtra(userCodes.LAST_NAME.toString())
            var email = intent.getStringExtra(userCodes.EMAIL.toString())
            var password = intent.getStringExtra(userCodes.PASSWORD.toString())
            var img = intent.getStringExtra(userCodes.PROFILE_IMG.toString())
             userID = intent.getStringExtra(userCodes.User_Id.toString())

//            binding.profileImg.setImageURI(Uri.parse(img))
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image while loading (optional)

            binding.pageTitle.setText("Update Page")
            profileUri = Uri.parse(img)
            // Load the image using Glide
            user = User(
                img,
                first.toString(),
                last.toString(),
                email.toString(),
                password.toString(),
                userID

            )
            Glide.with(this)
                .load(profileUri)
                .apply(requestOptions)
                .into(binding.profileImg)
            binding.firstnameid.setText(first)
            binding.lastnameid.setText(last)
            binding.emailSignup.setText(email)
            binding.passwordId.setText(password)
            binding.SignUp.setText("UpdateUser")
            binding.confirm.visibility = View.GONE

        }else{
            Log.d(TAG, "handleUserNavigation: signUpscreen else $direction ")
        }
    }

    private fun updateUser(user: User){
//        Log.e(TAG+345, "updateUser: ${user.userid}>> $userID", )
        val updatedData = hashMapOf(
            "firstName" to binding.firstnameid.text.toString(),
            "lastName" to binding.lastnameid.text.toString(),
            "email" to binding.emailSignup.text.toString(),
            "password" to binding.passwordId.text.toString(),
            "img" to profileUri,
            "userId" to userID

        )
        myfireobj.db.collection("users").document(user.userid.toString())
            .set(updatedData, SetOptions.merge())
            .addOnCompleteListener {
                   onBackPressed()
                this.finish()
                startActivity(Intent(this,LoggedUserDetails::class.java))

                Log.e("bitmapHandle", "updateUser: ${user.firstName},${user.email}", )
                updateStorage(user.email, profileUri!!,)

            }
            .addOnFailureListener {
                Log.e("bitmapHandle", "updateUser: exp : >>$it", )
            }

    }
    private fun updateStorage(oldFile:String,newFileUri:Uri){
        storageRef.child("images/$oldFile").putFile(newFileUri)
            .addOnCompleteListener {
//                Log.d("updateStorage12", "updateStorage: file updated ${newFileUri.toFile().name}")
                Log.d("updateStorage12", "if updateStorage: storage updated ")

            }
            .addOnFailureListener {
                Log.d("updateStorage12", "else updateStorage: file updated ${it}")
            }
    }

    private fun uriToFile(context: Context,uri: Uri):File?{


        var inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream!=null){
            try {
//                val randomInt = Random.nextInt(1, 101)
                var date = Date()
                var file = File(context.cacheDir,"$date")
                var outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                outputStream.close()
                return file
            }
            catch (e:Exception){
                Log.d(TAG, "uriToFile: $e")
            }
            finally {
                inputStream.close()
            }
        }
        return null
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

                Toast.makeText(this, "granted true", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "granted false", Toast.LENGTH_SHORT).show()

            }
        }

}