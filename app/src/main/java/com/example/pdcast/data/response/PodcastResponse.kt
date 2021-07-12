package com.example.pdcast.data.response

import com.google.gson.annotations.SerializedName

data class PodcastResponse (

    @SerializedName("resultCount") var resultCount : Int,
    @SerializedName("results") var results : List<Results>

)

data class Results (

    @SerializedName("wrapperType") var wrapperType : String,
    @SerializedName("kind") var kind : String,
    @SerializedName("artistId") var artistId : Int,
    @SerializedName("collectionId") var collectionId : Int,
    @SerializedName("trackId") var trackId : Int,
    @SerializedName("artistName") var artistName : String,
    @SerializedName("collectionName") var collectionName : String,
    @SerializedName("trackName") var trackName : String,
    @SerializedName("collectionCensoredName") var collectionCensoredName : String,
    @SerializedName("trackCensoredName") var trackCensoredName : String,
    @SerializedName("artistViewUrl") var artistViewUrl : String,
    @SerializedName("collectionViewUrl") var collectionViewUrl : String,
    @SerializedName("feedUrl") var feedUrl : String,
    @SerializedName("trackViewUrl") var trackViewUrl : String,
    @SerializedName("artworkUrl30") var artworkUrl30 : String,
    @SerializedName("artworkUrl60") var artworkUrl60 : String,
    @SerializedName("artworkUrl100") var artworkUrl100 : String,
    @SerializedName("collectionPrice") var collectionPrice : Int,
    @SerializedName("trackPrice") val trackPrice : Int,
    @SerializedName("trackRentalPrice") val trackRentalPrice : Int,
    @SerializedName("collectionHdPrice") val collectionHdPrice : Int,
    @SerializedName("trackHdPrice") val trackHdPrice : Int,
    @SerializedName("trackHdRentalPrice") var trackHdRentalPrice : Int,
    @SerializedName("releaseDate") var releaseDate : String,
    @SerializedName("collectionExplicitness") var collectionExplicitness : String,
    @SerializedName("trackExplicitness") var trackExplicitness : String,
    @SerializedName("trackCount") var trackCount : Int,
    @SerializedName("country") var country : String,
    @SerializedName("currency") var currency : String,
    @SerializedName("primaryGenreName") var primaryGenreName : String,
    @SerializedName("contentAdvisoryRating") var contentAdvisoryRating : String,
    @SerializedName("artworkUrl600") var artworkUrl600 : String,
    @SerializedName("genreIds") var genreIds : List<String>,
    @SerializedName("genres") var genres : List<String>

)