package com.example.chatapplication.app

import android.app.Application
import com.example.chatapplication.rtm.ChatManager
import dagger.hilt.android.HiltAndroidApp

class ChatApp : Application() {
    private var mChatManager: ChatManager? = null

    companion object {
        private var sInstance: ChatApp? = null
        fun the(): ChatApp? {
            return sInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        mChatManager = ChatManager(this)
        mChatManager?.init()
    }

    fun getChatManager(): ChatManager? {
        return mChatManager
    }

    override fun onTerminate() {
        super.onTerminate()
        mChatManager = null
    }
}