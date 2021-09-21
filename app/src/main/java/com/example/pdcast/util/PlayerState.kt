package com.example.pdcast.util

sealed class PlayerState<out T> {
    class None<out T>: PlayerState<T>()
    class Playing<out T> : PlayerState<T>()
    class Paused<out T> : PlayerState<T>()
    data class Failure<out T>(val throwable: Throwable) : PlayerState<T>()
}