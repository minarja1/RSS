package cz.minarik.nasapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.minarik.nasapp.utils.AppStartWorker
import cz.minarik.nasapp.utils.UpdateArticlesWorker

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appStartRoutine()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun appStartRoutine() {
        AppStartWorker.run(this)
        UpdateArticlesWorker.run(this)
    }
}