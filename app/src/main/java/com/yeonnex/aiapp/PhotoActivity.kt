package com.yeonnex.aiapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.renderscript.Element
import android.util.Log
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


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
                val resized_bitmap = resizeBitmap(bitmap)
                processTFLight(this, toGrayByteBuffer(resized_bitmap)) // 비트맵을 바이트 버퍼로 만드는 법...?
            }catch (e:Exception) { Log.e(TAG, "getBitmapException ${e.message}", e)
            }
        }
    }
    private fun resizeBitmap(bitmap: Bitmap):Bitmap{
        bitmap.createScaleBitmap(bitmap, 28, 28,false)
    }
    pvivate fun processTFLight(context: Context, byteBuffer: ByteBuffer){
        val model = KerasModel.newInstance(context)

        val inputFeatured0 = TensorBuffer.createFixelSize(intArrayOf(1,28,28), Element.DataType.FLOAT_32)

    }
    private fun toGrayByteBuffer(bitmap: Bitmap): ByteBuffer {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(28,28, ResizeOp.ResizedMethod.BILENEAR))
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(bitmap.byteCount)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = intArrayOf(bitmap.width * bitmap*height) // 리스트와 배열의 자료구조는 다름!!
        bitmap.getPixels(pixels, 0, bitmap.width, 0,0,bitmap.width, bitmap.height)

        pixels.forEach{pixel ->
            val r = pixel >> 16 & 0xFF
        }

        return byteBuffer
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
