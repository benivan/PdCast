package com.example.pdcast.util

import android.util.Log
import com.example.pdcast.data.response.RssFeedResponse
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.math.log

fun convertDataToModel(data: String) :RssFeedResponse{


    val streamData:InputStream = ByteArrayInputStream(data.toByteArray())

    val factory:XmlPullParserFactory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(streamData,null)

    var event:Int = parser.eventType
    while (event != XmlPullParser.END_DOCUMENT){
        var tagName = parser.name
        when(event){
            XmlPullParser.END_TAG -> {
                if (tagName == "title"){
//                    var titleName = parser.getAttributeValue(0)
                    Log.d(TAG, "convertDataToModel: ${event}")
                }
            }
        }
        event = parser.next()
    }




//    Log.d(TAG, "convertDataToModel: $data")

    return RssFeedResponse()


}



private const val TAG = "data"