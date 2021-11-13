package cz.minarik.nasapp.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


object RealtimeDatabaseHelper {

    private const val newsKey = "rssFeeds"

    fun getNewsFeeds(
        listener: RealtimeDatabaseQueryListener<List<RssFeedDTO>>,
        onSuccess: (() -> Unit)?
    ) {
        val databaseRef =
            Firebase.database("https://spacenews-9a76c-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(newsKey)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val feedDTO = dataSnapshot.getValue<List<RssFeedDTO>>()
                listener.onDataChange(feedDTO, onSuccess)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                listener.onCancelled(error)
            }
        })
    }


}

interface RealtimeDatabaseQueryListener<T> {
    fun onDataChange(data: T?, onSuccess: (() -> Unit)?)

    fun onCancelled(error: DatabaseError)
}