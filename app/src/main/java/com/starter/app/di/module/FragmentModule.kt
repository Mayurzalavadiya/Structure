package com.starter.app.di.module

import androidx.fragment.app.Fragment
import com.starter.app.ui.base.BaseFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    @FragmentScoped
    fun provideBaseFragment(fragment: Fragment): BaseFragment<*> {
        return fragment as BaseFragment<*>
    }

}