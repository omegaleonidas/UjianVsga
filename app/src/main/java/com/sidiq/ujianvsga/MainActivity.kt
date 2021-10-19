package com.sidiq.ujianvsga

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_REQUEST_CODE = 2020
    private val REQUEST_PERMISSION = 201
    private lateinit var dbHelper: DbHelper
    private lateinit var rgSex: RadioGroup
    private lateinit var rbSex: RadioButton
    private var sex = ""
    private var hasilFhoto = ""
    val GALLERY = 1
    val CAMERA = 2
    var uriPath: Uri? = null

    private var gender = "laki - laki"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        rgSex = findViewById(R.id.rgSex)
        dbHelper = DbHelper(this)


        btnLokasi.setOnClickListener {

            //check permission
            if (ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_REQUEST_CODE
                )
            } else {


                getCurrentLocation()


            }

        }
        btnImage.setOnClickListener {
            checkPermission()
            showPictureDialog()

        }
        btnSimpan.setOnClickListener {

            submit()

        }

    }


    fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCurrentLocation() {

        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val geocoder = Geocoder(this, Locale.getDefault())

        var addresses: List<Address>


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return

        }


        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        val locIndex = locationResult.locations.size - 1

                        val latitude = locationResult.locations.get(locIndex).latitude
                        val longitude = locationResult.locations.get(locIndex).longitude


//                        tvLatitude.text = "$latitude"

                        Log.e("latitude", "$latitude")
//                        tvLongitude.text = "Longitude: " + longitude

                        addresses = geocoder.getFromLocation(latitude, longitude, 1)


                        val address: String = addresses[0].getAddressLine(0)
                        tvAlamat.setText(address)

                        // lokasi = address


                    }
                }
            }, Looper.getMainLooper())


    }



    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {


        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data


                val selectedImageUri: Uri = data?.data!!
                val projection = arrayOf(MediaStore.Images.Media.DATA)

                try {
                    val cursor: Cursor? =
                        contentResolver.query(selectedImageUri, projection, null, null, null)
                    cursor?.moveToFirst()
                    val columnIndex: Int? = cursor?.getColumnIndex(projection[0])
                    val picturePath: String? = columnIndex?.let { cursor?.getString(it) }
                    cursor?.close()
                    hasilFhoto = picturePath.toString()
                    Log.d("Picture Path", "$picturePath")
                } catch (e: Exception) {
                    Log.e("Path Error", e.toString())
                }



                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        contentURI
                    )
                    saveImage(bitmap)
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()

                    imageView2!!.setImageBitmap(bitmap)


                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            imageView2!!.setImageBitmap(thumbnail)
            saveImage(thumbnail)

            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveImage(myBitmap: Bitmap) {
        val path = uri(myBitmap)
        uriPath = Uri.parse(path)

        Log.e("data fhoto", "onActivityResult: $path ")
    }



    fun uri(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
// have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdir()
        } else {
            Log.e("folder", " " + wallpaperDirectory.toString())
        }
        val f = File(
            wallpaperDirectory, ((Calendar.getInstance()
                .getTimeInMillis()).toString() + ".jpg")
        )
        try {
            Log.d("heel", wallpaperDirectory.toString())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.getPath()),
                arrayOf("image/jpeg"), null
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

            fo.close()
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())

            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return f.getAbsolutePath()


    }






    companion object {
        private val IMAGE_DIRECTORY = "/fhoto_pegawai"
    }


    private fun submit() {

        val selected = rgSex.checkedRadioButtonId
        rbSex = findViewById(selected)
        sex = rbSex.text.toString().trim()
        if (tvName.text.toString() == "" || tvNoHp.text.toString() == "" || tvAlamat.text.toString() == "" || sex == "" ) {
            Toast.makeText(this, "Silahkan isi data dengan lengkap", Toast.LENGTH_SHORT).show()
        } else {
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(DbHelper.COLUMN_NAME, tvName.text.toString().trim())
                put(DbHelper.COLUMN_ADDRESS, tvAlamat.text.toString().trim())
                put(DbHelper.COLUMN_NO_HP, tvNoHp.text.toString().trim())
                put(DbHelper.COLUMN_SEX, sex)

                put(DbHelper.COLUMN_PHOTO,hasilFhoto )
            }

            db.insert(DbHelper.TABLE_SQLite, null, values)
            val intent = Intent(this, TableActivity::class.java)
            startActivity(intent)
        }
    }


}