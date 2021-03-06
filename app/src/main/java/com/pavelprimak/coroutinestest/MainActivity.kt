package com.pavelprimak.coroutinestest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
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

    private val btnStart: Button by lazy {
        findViewById<Button>(R.id.btn_start_job)
    }

    private val btnStop: Button by lazy {
        findViewById<Button>(R.id.btn_stop_job)
    }

    private val textView: TextView by lazy {
        findViewById<TextView>(R.id.text)
    }

    private var job: Job? = null

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

        btnStart.setOnClickListener {
            startJob()
        }
        btnStop.setOnClickListener {
            stopJob()
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
                    .url("https://lookup.binlist.net/45717360")
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

    private fun startJob() {
        val startTime = System.currentTimeMillis()
        if (job == null)
        job = launch {
            var nextPrintTime = startTime
            var i = 0
            while (isActive) {
                if (System.currentTimeMillis() >= nextPrintTime) {
                    launch(UI) { textView.text = i++.toString() }
                    nextPrintTime += 1000L
                }
            }
        }
    }

    private fun stopJob() {
        launch(UI) {
            job?.cancelAndJoin()
            job = null
            textView.text = "Cancelled"
        }
    }
}
