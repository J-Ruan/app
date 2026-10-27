package com.warmcycle

import android.app.Application
import androidx.room.Room
import com.warmcycle.data.*

class WarmCycleApp : Application() {
    val repository by lazy { Repository(Room.databaseBuilder(this, WarmDatabase::class.java, "warmcycle.db").build(), Preferences(this)) }
}
