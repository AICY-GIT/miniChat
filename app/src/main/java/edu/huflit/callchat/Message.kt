package edu.huflit.callchat

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class Message {
    var text: String? = null
    var time:String?=null
    var senderId:String?=null

    constructor(){}
    constructor(text: String?, senderId:String?, time: String){
        this.text=text
        this.time=time
        this.senderId=senderId
    }
    fun getLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}