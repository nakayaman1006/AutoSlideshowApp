package jp.techacademy.yuuki.nakayama.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    // ユーザのパーミッションの許可の選択結果を受け取る
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        var fieldIndex:Int
        var id:Long
        var imageUri:Uri

        var mTimer: Timer? = null
        var mHandler = Handler()

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            id = cursor.getLong(fieldIndex)
            imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            Log.d("ANDROID", "URI : " + imageUri.toString())
            imageView.setImageURI(imageUri)

            // 進むボタンで1つ先の画像を表示
            next_button.setOnClickListener {

                if (mTimer == null) { // 自動送りの間は、進むボタンタップ不可
                    //最後の画像の表示時に、進むボタンをタップすると、最初の画像が表示
                    if (!cursor!!.moveToNext()) {
                        cursor.moveToFirst()
                    }
                    fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    id = cursor.getLong(fieldIndex)
                    imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    imageView.setImageURI(imageUri)
                }
            }

            // 戻るボタンで1つ前の画像を表示
            prev_button.setOnClickListener {

                if (mTimer == null) { // 自動送りの間は、戻るボタンタップ不可
                    // 最初の画像の表示時に、戻るボタンをタップすると、最後の画像が表示
                    if (!cursor!!.moveToPrevious()) {
                        cursor.moveToLast()
                    }
                    fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    id = cursor.getLong(fieldIndex)
                    imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    imageView.setImageURI(imageUri)
                }
            }

            // 再生ボタンをタップすると2秒後に自動送りが始まり、2秒毎にスライドさせる
            start_reset_button.setOnClickListener {

                if (mTimer == null) {
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {

                            mHandler.post {
                                if (!cursor!!.moveToNext()) {
                                    cursor.moveToFirst()
                                }
                                fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                id = cursor.getLong(fieldIndex)
                                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                                imageView.setImageURI(imageUri)
                            }
                        }
                    }, 2000, 2000)
                } else {
                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        }

    }
}