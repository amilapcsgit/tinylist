package com.cyberlist.neonlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cyberlist.neonlist.ui.NeonTheme
import com.cyberlist.neonlist.ui.NeonListApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val app = application as NeonListApplication

    setContent {
      val vm: AppViewModel = viewModel(factory = AppViewModelFactory(app.repository))
      NeonTheme {
        NeonListApp(vm)
      }
    }
  }
}
