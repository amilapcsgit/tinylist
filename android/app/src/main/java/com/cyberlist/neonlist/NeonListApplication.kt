package com.cyberlist.neonlist

import android.app.Application
import com.cyberlist.neonlist.BuildConfig
import com.cyberlist.neonlist.data.NeonDatabase
import com.cyberlist.neonlist.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class NeonListApplication : Application() {
  lateinit var repository: Repository
    private set

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
    val db = NeonDatabase.getInstance(this)
    repository = Repository(this, db.listDao(), db.itemDao(), db)

    CoroutineScope(Dispatchers.IO).launch {
      repository.seedIfEmpty()
    }
  }
}
