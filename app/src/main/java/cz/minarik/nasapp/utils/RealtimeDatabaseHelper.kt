package cz.minarik.nasapp.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


object RealtimeDatabaseHelper {

    private const val newsKey = "rssFeeds"

    fun getNewsFeeds(listener: RealtimeDatabaseQueryListener<List<RssFeedDTO>>) {
        val databaseRef = Firebase.database.getReference(newsKey)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val feedDTO = dataSnapshot.getValue<List<RssFeedDTO>>()
                listener.onDataChange(feedDTO)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                listener.onCancelled(error)
            }
        })
    }


    data class RssFeedDTO(
        val title: String? = null,
        val url: String? = null,
    )
}

interface RealtimeDatabaseQueryListener<T> {
    fun onDataChange(data: T?)

    fun onCancelled(error: DatabaseError)
}