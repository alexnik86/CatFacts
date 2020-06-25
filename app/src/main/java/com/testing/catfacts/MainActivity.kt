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

const val CAT_FACT_URL : String = "http://cat-fact.herokuapp.com/facts";
const val RANDOM_CAT_PHOTO_URL : String = "http://aws.random.cat/meow";

class MainActivity : AppCompatActivity() {

    private var jobDownloadPhoto: Job? = null
    private var currentPosition = 0
    private var allCatFacts: AllCatFacts = AllCatFacts()
    private var gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(CAT_FACT_URL)
            val urlConnection = url.openConnection() as HttpURLConnection
            var catFacts: AllCatFacts
            try {
                val reader = BufferedReader(urlConnection.inputStream.reader())
                catFacts = gson.fromJson(reader, AllCatFacts::class.java) as AllCatFacts
            } finally {
                urlConnection.disconnect();
            }
            withContext(Dispatchers.Main) {
                allCatFacts = catFacts
                showNextFact()
            }
        }

        butNext.setOnClickListener {
            currentPosition++;
            showNextFact()
        }

        butPrev.setOnClickListener {
            currentPosition--;
            showNextFact()
        }
    }

    private fun showNextFact() {
        setNextCatFact()
        setRandomPhotoCat()
    }

    private fun setNextCatFact() {

        if (currentPosition>allCatFacts.all.lastIndex - 1) {
            currentPosition=0
        } else if (currentPosition==-1) {
            currentPosition = allCatFacts.all.lastIndex - 1
        }
        val curPos = currentPosition + 1
        val allPos = allCatFacts.all.lastIndex
        val curAndAllFacts = "$curPos/$allPos";
        text_cat_fact_num.text = curAndAllFacts
        text_cat_fact.text = allCatFacts.all!![currentPosition].text
    }

    private fun setRandomPhotoCat() {

        progress.visibility = View.VISIBLE
        img_cat_photo.visibility = View.GONE

        if (jobDownloadPhoto?.isActive == true) {
            jobDownloadPhoto?.cancel();
        }

        jobDownloadPhoto = GlobalScope.launch {
            withContext(Dispatchers.IO) {

                val message = "Loading photo for fact num $currentPosition"
                withContext(Dispatchers.Main) {
                    showToast(message)
                }

                val url = URL(RANDOM_CAT_PHOTO_URL)
                var urlConnPicture: HttpURLConnection? = null
                val urlConnection = url.openConnection() as HttpURLConnection
                var jsonObj: JsonObject
                try {
                    val reader = BufferedReader(urlConnection.inputStream.reader())
                    jsonObj = gson.fromJson(reader, JsonObject::class.java)

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

    private fun showToast(text: String) {
        Toast.makeText(applicationContext,text,Toast.LENGTH_SHORT).show()
    }

    private fun showPicture(bitmap: Bitmap) {
        img_cat_photo.setImageBitmap(bitmap)
    }
}
