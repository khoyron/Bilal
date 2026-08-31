package org.khoyron.bilal.di

import org.khoyron.bilal.util.AudioPlayer
import org.khoyron.bilal.util.IosAudioPlayer
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.bind

import org.khoyron.bilal.ui.qiblah.QiblahSensorManager
import org.khoyron.bilal.ui.qiblah.IosQiblahSensorManager

import org.khoyron.bilal.util.PlatformActions
import org.khoyron.bilal.util.IosPlatformActions

actual fun platformModule(): Module = module {
    single { IosAudioPlayer() } bind AudioPlayer::class
    single<QiblahSensorManager> { IosQiblahSensorManager() }
    single<PlatformActions> { IosPlatformActions() }
}
