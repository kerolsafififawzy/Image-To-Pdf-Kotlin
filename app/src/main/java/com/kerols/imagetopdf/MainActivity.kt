package com.kerols.imagetopdf

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.kerols.pdfconverter.CallBacks
import com.kerols.pdfconverter.ImageToPdf
import com.kerols.pdfconverter.PdfImageSetting
import com.kerols.pdfconverter.PdfPage
import java.io.File

class MainActivity : AppCompatActivity() {
    private var Cancel: MaterialButton? = null
    private var GetImage: MaterialButton? = null
    private var imageToPdf: ImageToPdf? = null
    private val TAG = MainActivity::class.java.toString()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Cancel = findViewById(R.id.Cancel)
        GetImage = findViewById(R.id.GetImage)
        Cancel!!.setEnabled(false)

        // Setting for the page
        val pdfPage = PdfPage(applicationContext)
        pdfPage.setPageSize(1000, 1000)

        // Setting for a single image on a page
        val mPdfImageSetting = PdfImageSetting()
        mPdfImageSetting.setImageSize(200, 200)
        mPdfImageSetting.setMargin(20, 20, 20, 20)

        // Setting for the second image on the page
        val mPdfImageSetting2 = PdfImageSetting()
        mPdfImageSetting2.setImageSize(100, 100)
        mPdfImageSetting2.setMargin(220, 220, 220, 220)


        // Add photos that are set in one page
        pdfPage.add(mPdfImageSetting)
        pdfPage.add(mPdfImageSetting2)
        imageToPdf = ImageToPdf(pdfPage, applicationContext)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            1
        )
        Cancel!!.setOnClickListener(View.OnClickListener {
            if (imageToPdf != null) {
                imageToPdf!!.Cancel()
            }
        })
        GetImage!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (GetImage != null) {
            GetImage!!.isEnabled =
                grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            assert(data != null)

            // Use one of the method for convert To File PDf
            imageToPdf!!.DataToPDF(
                data!!,
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "ImageToPdf.pdf"
                ), object : CallBacks {
                    override fun onFinish(path: String) {
                        Cancel!!.isEnabled = false
                        GetImage!!.isEnabled = true
                        Toast.makeText(applicationContext, "onFinish", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable) {
                        Cancel!!.isEnabled = false
                        GetImage!!.isEnabled = true
                        Toast.makeText(applicationContext, "onError", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "onError: ", throwable)
                    }

                    override fun onProgress(progress: Int, max: Int) {
                        Log.e(TAG, "onProgress: $progress  $max")
                    }

                    override fun onCancel() {
                        Cancel!!.isEnabled = false
                        GetImage!!.isEnabled = true
                        Toast.makeText(applicationContext, "onCancel", Toast.LENGTH_SHORT).show()
                    }

                    override fun onStart() {
                        Cancel!!.isEnabled = true
                        GetImage!!.isEnabled = false
                        Toast.makeText(applicationContext, "onStart", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}