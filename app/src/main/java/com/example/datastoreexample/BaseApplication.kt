package com.example.datastoreexample

import android.app.Application
import com.example.datastoreexample.injection.ApplicationComponent

class BaseApplication: Application() {
  companion object {
    lateinit var instance: BaseApplication
  }

  init {
    instance = this
  }

  override fun onCreate() {
    super.onCreate()
  }
}