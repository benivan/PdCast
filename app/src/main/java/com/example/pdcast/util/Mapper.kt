package com.example.pdcast.util

interface Mapper<E,M> {
    fun mapModelToEntity(model:M):E
    fun mapEntityToModel(entity:E):M
}