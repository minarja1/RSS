package cz.minarik.nasapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.minarik.base.common.prefs.PrefManager
import cz.minarik.nasapp.R
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
