package com.cyberlist.neonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cyberlist.neonlist.data.Repository

class AppViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return AppViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
