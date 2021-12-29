package com.yeonnex.aiapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.File


class PhotoActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "==========="
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        btnPhoto.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Photo"), 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK) {
            var imageUri = data?.data
            imageUri?.let {
                Log.d(TAG, "image URI : ${uri2path(this, imageUri)}")
            }
            try {
                var bimap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                imageView.setImageBitmap(bitmap)
            }catch (e:Exception) { Log.e(TAG, "getBitmapException ${e.message}", e)
            }
        }
    }

    //Uri -> Path(파일경로)
    fun uri2path(context: Context, contentUri: Uri): String {
        var path: String = ""
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        context.getContentResolver().query(
            contentUri, proj, null, null, null)?.run {
            moveToNext()
            path = getString(getColumnIndex(MediaStore.MediaColumns.DATA))
            val uri = Uri.fromFile(File(path))
        }
        return path
    }
}
