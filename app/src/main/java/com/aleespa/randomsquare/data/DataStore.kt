import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Create this once in your project (e.g., DataStore.kt)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")