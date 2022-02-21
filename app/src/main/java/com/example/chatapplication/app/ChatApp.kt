package com.example.chatapplication.app

import android.app.Application
import com.example.chatapplication.rtm.ChatManager

class ChatApp : Application() {
    private var sInstance: ChatApp? = null
    private var mChatManager: ChatManager? = null


    fun the(): ChatApp? {
        return sInstance
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        mChatManager = ChatManager()
        mChatManager?.init()
    }

    fun getChatManager(): ChatManager? {
        return mChatManager
    }
}