package com.example.pdcast.ui

import android.os.Parcelable
import androidx.lifecycle.ViewModel

class HomeViewModel:ViewModel() {

    private lateinit var state: Parcelable
    fun saveRecyclerViewState(parcelable: Parcelable) { state = parcelable }
    fun restoreRecyclerViewState() : Parcelable = state
    fun stateInitialized() : Boolean = ::state.isInitialized
}