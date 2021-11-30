package com.example.datastoreexample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.datastoreexample.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MainActivity : AppCompatActivity() {
  private lateinit var viewBinding: ActivityMainBinding

  companion object {
    private val AVAILABLE_TAG = listOf<String>(
      "free", "limited", "out of stock", "price up", "exclusive", "flash sale", "cod"
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    initView()
    AVAILABLE_TAG.forEach {
      val textView = TextView(this@MainActivity)
      textView.text = "- $it"
      viewBinding.llTags.addView(textView)
    }

    //    flow<String> {
    //      while (true) {
    //        emit(getRandomTags())
    //        delay(3000)
    //      }
    //    }.asLiveData().observe(this, {
    //      Toast.makeText(this, "Test flow: $it", Toast.LENGTH_SHORT).show()
    //    })
  }

  private fun initView() {
    with(viewBinding) {
      btSave.setOnClickListener { //Preference DataStore
        lifecycleScope.launchWhenResumed {
          dataStore.edit {
            it[intPreferencesKey("coba_int")] = etInput.text.toString().toIntOrNull() ?: 0
          }
          showSuccessToast()
        }

        //        getSharedPreferences("shared_preference_name", Context.MODE_PRIVATE).edit()
        //          .putString("name", etInput.text.toString()).commit()

        //        runBlocking {
        //          dataStore.edit {
        //            it[intPreferencesKey("coba_int")] = etInput.text.toString().toIntOrNull() ?: 0
        //          }
        //        }

        //Proto DataStore
        //        lifecycleScope.launchWhenResumed {
        //          setProductName(etInput.text.toString())
        //        }
      }

      btRetrieve.setOnClickListener { //Preference DataStore
        //        val preferenceKey = intPreferencesKey("preference_key")
        //        val intFlow: Flow<Int> = dataStore.data.map {
        //          it[preferenceKey] ?: 0
        //        }
        //        intFlow.asLiveData().observe(this@MainActivity, { data ->
        //          // do something with the data
        //        })
        //        lifecycleScope.launch {
        //          val result = intFlow.first()
        //          // do something with the data
        //          withContext(Dispatchers.Main) {
        //            // do something with the UI & data if required
        //          }
        //        }
        //        val result: Int = runBlocking {
        //          intFlow.first()
        //        }

        val intFlow: Flow<Int> = dataStore.data.map {
          it[intPreferencesKey("coba_int")] ?: 123
        }
        lifecycleScope.launchWhenResumed {
          val result = intFlow.first()
          withContext(Dispatchers.Main) {
            viewBinding.tvIntro.text = "int saved: $result"
          }
        }

        //Proto DataStore
        //        lifecycleScope.launchWhenResumed {
        //          val result = getProductName().first()
        //          withContext(Dispatchers.Main) {
        //            tvIntro.text = "Nama: $result"
        //          }
        //        }
      }

      //test proto data list
      btClearList.setOnClickListener {
        lifecycleScope.launchWhenResumed {
          productDataStore.updateData {
            it.toBuilder().clearTags().build()
          }
        }
      }

      btAddList.setOnClickListener {
        lifecycleScope.launchWhenResumed {
          productDataStore.updateData {
            it.toBuilder().addTags(getRandomTags()).build()
          }
          showSuccessToast("success added")
        }
      }

      btShowList.setOnClickListener {
        lifecycleScope.launchWhenResumed {
          val result = productDataStore.data.map {
            it.tagsList.joinToString(", ")
          }.first()
          withContext(Dispatchers.Main) {
            tvIntro.text = "Tags saved: $result"
          }
        }
      }

      //test proto nested object
      btSaveAddOn.setOnClickListener {
        lifecycleScope.launchWhenResumed {
          productDataStore.updateData {
            it.toBuilder().setAddOn(Product.AddOn.newBuilder().setName(etInput.text.toString()))
              .build()
          }
          showSuccessToast()
        }
      }

      btShowAddOn.setOnClickListener {
        lifecycleScope.launchWhenResumed {
          val result = productDataStore.data.map {
            it.addOn.price
          }.first()

          withContext(Dispatchers.Main) {
            tvIntro.text = "addon name saved: $result"
          }
        }
      }
    }
  }

  private suspend fun setProductName(name: String) {
    productDataStore.updateData {
      it.toBuilder().setName(name).build()
    }
    withContext(Dispatchers.Main) {
      Toast.makeText(this@MainActivity, "Nama produk: $name sukses disimpan", Toast.LENGTH_SHORT)
        .show()
    }
  }

  private fun getProductName(): Flow<String> {
    return productDataStore.data.map {
      it.name ?: "name default value"
    }
  }

  private suspend fun showSuccessToast(text: String = "Success saved") {
    withContext(Dispatchers.Main) {
      Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
    }
  }

  private fun getRandomTags(): String = AVAILABLE_TAG[AVAILABLE_TAG.indices.random()]
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_data_store")
val Context.productDataStore: DataStore<Product> by dataStore(fileName = "product_data_store.pb",
  serializer = CustomSerializer,
  produceMigrations = { context ->
    listOf(SharedPreferencesMigration(
      context = context, sharedPreferencesName = "shared_preference_name"
    ) { sharedPrefs: SharedPreferencesView, currentData: Product ->
      currentData.toBuilder().setName(sharedPrefs.getString("name", "unspecified")!!).build()
    },
      SharedPreferencesMigration(
        context = context,
        sharedPreferencesName = "shared_preference_kedua"
      ) { sharedPrefs: SharedPreferencesView, currentData: Product ->
        currentData
      })
  })

enum class UiMode {
  LIGHT, DARK
}