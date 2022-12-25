package org.dianqk.ruslin

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.dianqk.ruslin.data.NotesRepository
import org.dianqk.ruslin.di.ApplicationScope
import javax.inject.Inject

@HiltAndroidApp
class RuslinApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notesRepository: NotesRepository

//    @Inject
//    @ApplicationScope
//    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
//        applicationScope.launch {
//
//        }
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}
