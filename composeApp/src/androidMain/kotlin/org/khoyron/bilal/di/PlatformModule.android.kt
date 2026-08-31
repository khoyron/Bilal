package org.khoyron.bilal.di

import org.khoyron.bilal.util.AudioPlayer
import org.khoyron.bilal.util.AndroidAudioPlayer
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.bind
import org.koin.core.module.dsl.singleOf

import org.khoyron.bilal.ui.qiblah.QiblahSensorManager
import org.khoyron.bilal.ui.qiblah.AndroidQiblahSensorManager

import org.khoyron.bilal.util.PlatformActions
import org.khoyron.bilal.util.AndroidPlatformActions

actual fun platformModule(): Module = module {
    single { AndroidAudioPlayer(get()) } bind AudioPlayer::class
    single<QiblahSensorManager> { AndroidQiblahSensorManager(get()) }
    single<PlatformActions> { AndroidPlatformActions(get()) }
}
