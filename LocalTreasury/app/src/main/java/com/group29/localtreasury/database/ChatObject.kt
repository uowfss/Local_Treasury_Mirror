package com.group29.localtreasury.database

class ChatObject {
    var senderID:String = ""
    var recieverID: String = ""
    var participants: MutableList<String> = mutableListOf()
    var messages: MutableList<String> = mutableListOf()
}