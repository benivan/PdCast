//package com.example.pdcast.di
//
//import android.content.Context
//import androidx.core.app.ServiceCompat
//import com.google.android.exoplayer2.C
//import com.google.android.exoplayer2.SimpleExoPlayer
//import com.google.android.exoplayer2.audio.AudioAttributes
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
//import com.google.android.exoplayer2.util.Util
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.android.scopes.ServiceScoped
//
//
//@Module
//@InstallIn(ServiceCompat::class)
//object ServiceModule {
//
//    @Provides
//    @ServiceScoped
//    fun providesAudioAttributes() = AudioAttributes.Builder()
//        .setContentType(C.CONTENT_TYPE_MUSIC)
//        .setUsage(C.USAGE_GAME)
//        .build()
//
//
//    @Provides
//    @ServiceScoped
//    fun provideExoPlayer(
//        @ApplicationContext context: Context,
//        audioAttributes: AudioAttributes
//    ) = SimpleExoPlayer.Builder(context).build().apply {
//        setAudioAttributes(audioAttributes,true)
//        setHandleAudioBecomingNoisy(true)
//    }
//
//
//    @Provides
//    @ServiceScoped
//    fun provideDateSourceFactory(
//        @ApplicationContext context: Context
//    ) =  DefaultDataSourceFactory(context,Util.getUserAgent(context,"Pd Cast"))
//}