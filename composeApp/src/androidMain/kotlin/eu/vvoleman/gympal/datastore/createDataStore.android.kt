package eu.vvoleman.gympal.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import eu.vvoleman.gympal.core.datastore.createDataStore
import eu.vvoleman.gympal.core.datastore.dataStoreFileName

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)