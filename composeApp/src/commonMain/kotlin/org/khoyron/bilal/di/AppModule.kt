package org.khoyron.bilal.di

import com.russhwolf.settings.Settings
import org.khoyron.bilal.data.local.SessionManager
import org.khoyron.bilal.data.repository.AzanRepositoryImpl
import org.khoyron.bilal.domain.repository.AzanRepository
import org.khoyron.bilal.domain.usecase.GetAzanTimesUseCase
import org.khoyron.bilal.data.network.httpClient
import org.khoyron.bilal.ui.azan.AzanViewModel
import org.khoyron.bilal.ui.quran.QuranViewModel
import org.khoyron.bilal.ui.quran.detail.SurahDetailViewModel
import org.khoyron.bilal.ui.qiblah.QiblahViewModel
import org.khoyron.bilal.ui.mosquefinder.MosqueFinderViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    // ── Local & Network ──────────────────────────────────────────────────
    single { httpClient }
    single { Settings() }
    singleOf(::SessionManager)

    // ── ViewModels ────────────────────────────────────────────────────────
    viewModelOf(::AzanViewModel)
    viewModelOf(::QuranViewModel)
    viewModelOf(::SurahDetailViewModel)
    viewModelOf(::QiblahViewModel)
    viewModelOf(::MosqueFinderViewModel)

    // ── Repositories / UseCases ───────────────────────────────────────────
    singleOf(::AzanRepositoryImpl) bind AzanRepository::class
    singleOf(::GetAzanTimesUseCase)
}
