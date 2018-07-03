package com.pavelprimak.coroutinestest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private val btn1: Button by lazy {
        findViewById<Button>(R.id.btn_launch)
    }

    private val btn2: Button by lazy {
        findViewById<Button>(R.id.btn_launch_with_error)
    }

    private val btn3: Button by lazy {
        findViewById<Button>(R.id.btn_launch_with_return_value)
    }
    private val textView: TextView by lazy {
        findViewById<TextView>(R.id.text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn1.setOnClickListener {
            launch(UI) {
                setTextAfterDelay(2, "hello corotines!! \nLaunch with suspend method!!!")
            }
        }

        btn2.setOnClickListener {
            launch(UI) {
                try {
                    async(CommonPool) { throwError() }.await()
                } catch (exception: IOException) {
                    // handle the exception here
                    textView.text = "ERROR"
                }
            }
        }
        btn3.setOnClickListener {
            launch(UI) {
                val data = downloadDataAsync()
                textView.text = data
            }
        }
    }

    private suspend fun setTextAfterDelay(seconds: Long, text: String) {
        delay(seconds, TimeUnit.SECONDS)
        textView.text = text
    }

    private fun throwError() {
        throw IOException()
    }

    private suspend fun downloadDataAsync(): String {
        return suspendCoroutine { cont ->
            val client = OkHttpClient()
            val request = Request.Builder()
                    .url("https://jsonplaceholder.typicode.com/posts")
                    .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    cont.resume(response.body()?.string() ?: "")
                }
            })
        }
    }
}
