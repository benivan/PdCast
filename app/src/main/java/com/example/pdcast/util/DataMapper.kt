package com.example.pdcast.util

import com.example.pdcast.data.model.ItunesPodcast
import com.example.pdcast.data.dto.DBPodcast

class DataMapper : Mapper<DBPodcast, ItunesPodcast> {
    override fun mapModelToEntity(model: ItunesPodcast): DBPodcast {
        return DBPodcast(
            collectionCensoredName = model.collectionCensoredName,
            feedUrl = model.feedUrl,
            artworkUrl100 = model.artworkUrl100,
            artworkUrl30 = model.artworkUrl30,
            releaseDate = model.releaseDate
        )

    }

    override fun mapEntityToModel(entity: DBPodcast): ItunesPodcast {
        return ItunesPodcast(
            collectionCensoredName = entity.collectionCensoredName,
            feedUrl = entity.feedUrl,
            artworkUrl30 = entity.artworkUrl30,
            artworkUrl100 = entity.artworkUrl100,
            releaseDate = entity.releaseDate
        )
    }
}