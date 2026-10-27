package com.warmcycle

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warmcycle.ui.*

class MainActivity : FragmentActivity() {
    private var unlocked by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        val vm = ViewModelProvider(this)[AppViewModel::class.java]
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            WarmTheme(state.settings.theme) {
                if (state.loading) LoadingScreen()
                else if (state.settings.lock && !unlocked) LockScreen(this) { unlocked = true }
                else WarmApp(vm, state)
            }
        }
    }
    override fun onStop() { super.onStop(); unlocked = false }
}
