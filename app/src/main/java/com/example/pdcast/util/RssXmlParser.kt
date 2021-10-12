package com.example.pdcast.util

import android.util.Log
import android.util.Xml
import com.example.pdcast.data.response.RssFeedResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RssXmlParser {


    private val ns: String? = null
    private var episodeImageUrl: String? = null
    private var podcastName:String? =null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): RssFeedResponse = inputStream.use { inputStream ->
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readEntry(parser)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun parseXml(inputStream: InputStream): RssFeedResponse =
        suspendCancellableCoroutine {
            inputStream.use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(inputStream, null)
                parser.nextTag()
                try {
                    it.resume(readFeed(parser))
                } catch (e: Exception) {
                    it.resumeWithException(e)
                }
            }
        }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): RssFeedResponse {

        var rssFeedResponse = RssFeedResponse()

        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "channel" -> rssFeedResponse = readEntry(parser)
                "item" -> rssFeedResponse.episodes.add(readItem(parser))
                else -> skip(parser)
            }
        }
        return rssFeedResponse
    }


    // Read Entry
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): RssFeedResponse {
        parser.require(XmlPullParser.START_TAG, ns, "channel")
        var title: String? = null
        var description: String? = null
        var link: String? = null

        var language: String? = null
        var imageUrl: String? = null
        val episodes = mutableListOf<RssFeedResponse.EpisodeResponse>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "title" -> title = readTitle(parser)
                "description" -> description = readDescription(parser)
                "link" -> link = readLink(parser)
                "language" -> language = readLanguage(parser)
                "itunes:image" -> imageUrl = readItunesImage(parser)
                else -> skip(parser)
            }
        }
        podcastName = title
        return RssFeedResponse(title, description, language, link, imageUrl, episodes)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readItunesImage(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "itunes:image")
        var imageUrl: String? = null

            if (parser.getAttributeName(0) == "href") {
                imageUrl = parser.getAttributeValue(0)
            }
        episodeImageUrl = imageUrl
        return imageUrl
    }


    //READ TEXT
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }


    //Read Title
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {

        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }


    // Read Description
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "description")
        val description = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "description")
        return description
    }


    // Read Link
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser): String {

        parser.require(XmlPullParser.START_TAG, ns, "link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "link")
        return link
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLanguage(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "language")
        val language = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "language")
        return language
    }


    //read Image url
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readImageUrl(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "image")
        var imageUrl: String = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "url" -> imageUrl = readIUrl(parser)
            }
        }
        episodeImageUrl = imageUrl
        return imageUrl
    }

    //read url
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readIUrl(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "url")
        val url = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "url")
        return url
    }

    //pubDate
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPubDate(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate")
        val pubDate = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "pubDate")
        return pubDate
    }

    //Skip Tag
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
//        Log.d(TAG, "skip: ${parser.name}")
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }


    // READ ITEM
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser): RssFeedResponse.EpisodeResponse {
        parser.require(XmlPullParser.START_TAG, ns, "item")

        var title: String? = null
        var link: String? = null
        var description: String? = null
        var pubDate: String? = null
        var episodeUrl: String? = null
        var duration: String? = null
        var imageUrl = episodeImageUrl

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "description" -> description = readDescription(parser)
                "link" -> link = readLink(parser)
                "pubDate" -> pubDate = readPubDate(parser)
                "enclosure" -> episodeUrl = readEpisodeUrl(parser)
                "itunes:duration" -> duration = readDuration(parser)
                else -> skip(parser)
            }
        }
        return RssFeedResponse.EpisodeResponse(title, link, description, pubDate,duration,episodeUrl,imageUrl,podcastName)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDuration(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "itunes:duration")
        val durationTime = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "itunes:duration")
        fun removeColon(duration: String):String{
            return if(duration.contains(":")){
                duration.replace(":","")
            } else duration
        }
        return removeColon(durationTime)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEpisodeUrl(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "enclosure")
        var episodeUrl: String? = null

        for (i in 0 until parser.attributeCount){
           if (parser.getAttributeName(i) == "url") {
               episodeUrl = parser.getAttributeValue(i)
               break
           }
        }
        parser.nextTag()
        return episodeUrl
    }

    companion object {
        private const val TAG = "RssXmlParser"
    }


}