package com.cyberlist.neonlist

import android.app.Application
import com.cyberlist.neonlist.data.NeonDatabase
import com.cyberlist.neonlist.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NeonListApplication : Application() {
  lateinit var repository: Repository
    private set

  override fun onCreate() {
    super.onCreate()
    val db = NeonDatabase.getInstance(this)
    repository = Repository(db.listDao(), db.itemDao())

    CoroutineScope(Dispatchers.IO).launch {
      repository.seedIfEmpty()
    }
  }
}
