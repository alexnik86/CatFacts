package com.testing.catfacts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {


    private var jobDownloadPhoto: Job? = null
    private var currentPosition: Int = 0
    private var catFacts: CatFacts? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        runBlocking {
            val deferred = GlobalScope.async(Dispatchers.IO) {
                val url = URL("http://cat-fact.herokuapp.com/facts")
                val urlConnection = url.openConnection() as HttpURLConnection
                var catFacts: CatFacts
                try {
                    val reader = BufferedReader(urlConnection.inputStream.reader())
                    val founderType = object : TypeToken<CatFacts>() {}.type
                    catFacts = Gson().fromJson(reader, founderType) as CatFacts
                } finally {
                    urlConnection.disconnect();
                }
                catFacts
                /*
                for (catFact in catFacts!!.all) {
                    Log.e("Cat fact", catFact.text)
                }
                */
            }

            catFacts = deferred.await()
        }

        showCatFact()

        butNext.setOnClickListener {
            currentPosition++;
            showCatFact()
        }

        butPrev.setOnClickListener {
            currentPosition--;
            showCatFact()
        }
    }

    private fun showCatFact() {

        if (currentPosition>catFacts?.all?.size!! - 1) {
            currentPosition=0
        } else if (currentPosition==-1) {
            currentPosition = catFacts?.all?.size!! - 1
        }
        val curPos = currentPosition + 1
        val allPos = catFacts?.all?.size!!
        text_cat_fact_num.text = "$curPos/$allPos"
        (findViewById<TextView>(R.id.text_cat_fact)).text = catFacts!!.all[currentPosition].text
        getRandomPhotoCat()
    }

    private fun getRandomPhotoCat() {

        progress.visibility = View.VISIBLE
        img_cat_photo.visibility = View.GONE

        if (jobDownloadPhoto?.isActive==true) {
            jobDownloadPhoto?.cancel();
        }

        runBlocking {
            jobDownloadPhoto = GlobalScope.launch {
                withContext(Dispatchers.IO) {


                    val message = getMessage(currentPosition)
                    withContext(Dispatchers.Main) {
                        showToast(message)
                    }

                    val url = URL("http://aws.random.cat/meow")
                    var urlConnPicture: HttpURLConnection? = null
                    val urlConnection = url.openConnection() as HttpURLConnection
                    var jsonObj: JsonObject
                    try {
                        val reader = BufferedReader(urlConnection.inputStream.reader())
                        jsonObj = Gson().fromJson(reader, JsonObject::class.java)

                        val urlPicture = URL(jsonObj["file"]!!.asString)
                        urlConnPicture = urlPicture.openConnection() as HttpURLConnection
                        var bitmap = BitmapFactory.decodeStream(urlConnPicture.inputStream)
                        withContext(Dispatchers.Main) {
                            progress.visibility = View.GONE
                            img_cat_photo.visibility = View.VISIBLE
                            showPicture(bitmap)
                        }

                    } finally {
                        urlConnection.disconnect();
                        urlConnPicture?.disconnect();
                    }





                }
            }
        }
    }

    private suspend fun getMessage(currentPosition: Int): String {
        return suspendCoroutine { continuation ->
            continuation.resume("Loading photo for fact num $currentPosition")
        }
    }


    private fun showToast(text: String) {
        Toast.makeText(applicationContext,text,Toast.LENGTH_SHORT).show()
    }

    private fun showPicture(bitmap: Bitmap) {
        img_cat_photo.setImageBitmap(bitmap)
    }
}
