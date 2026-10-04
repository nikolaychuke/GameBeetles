package com.example.beetles

import android.app.Application
import com.example.beetles.data.GameRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BeetlesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BeetlesApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { GameRepository(get()) }
    single { CurrencyRepository() }

    viewModel { GameViewModel() }
}