package com.sidiq.ujianvsga

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sidiq.ujianvsga.Model.models
import java.io.File

class TableActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private val LOCATION_PERMISSION_REQ_CODE = 1000
    private val REQUEST_CODE = 2020
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        askForPermissions()


        val tableLayout = findViewById<TableLayout>(R.id.tableLayoutForm)

        dbHelper = DbHelper(this)
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DbHelper.TABLE_SQLite}"
        val cursor = db.rawQuery(query, null)

        val list: MutableList<models> = ArrayList()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_NAME))
                val address = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ADDRESS))
                val noHp = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_NO_HP))
                val sex = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_SEX))
                val photo = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_PHOTO))
                val profile = models(id, name, address, noHp, sex,  photo)

                list.add(profile)
            } while (cursor.moveToNext())
        }

        for (i in list.indices) {
            val row: TableRow = layoutInflater.inflate(R.layout.layout_row, null) as TableRow
            val no = row.findViewById(R.id.noTabel) as TextView
            val nama = row.findViewById(R.id.namaTabel) as TextView
            val alamat = row.findViewById(R.id.alamatTabel) as TextView
            val noHp = row.findViewById(R.id.noHpTabel) as TextView
            val jenisKelamin = row.findViewById(R.id.jenisKelaminTabel) as TextView


            no.text = list[i].id
            nama.text = list[i].name
            alamat.text = list[i].lokasi
            noHp.text = list[i].no_hp
            jenisKelamin.text = list[i].jenisKelamin


            if (askForPermissions()) {
                val imgFile = File(list[i].photo)
                Log.d("TAG", "onCreate: ${imgFile.absoluteFile}")
                if (imgFile.exists()) {
                    val bitmapOption = BitmapFactory.Options()
                    bitmapOption.inSampleSize = 8
                    val myBitmap = BitmapFactory.decodeFile(imgFile.toString(), bitmapOption)
                    val photo = row.findViewById(R.id.photoTabel) as ImageView
                    photo.setImageBitmap(myBitmap)
                    Log.d("TAG", "onCreate: Jalan")
                }
            }

            tableLayout.addView(row)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                } else {
                    Toast.makeText(this, "You need to grant permission to access location", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    //  askForPermissions()
                }
                return
            }
        }
    }

    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(this as Activity,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_CODE)
            }
            return false
        }
        return true
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    // send to app settings if permission is denied permanently
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                })
            .setNegativeButton("Cancel",null)
            .show()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

}