package com.example.chatapplication.rtm

import android.content.Context
import android.util.Log
import com.example.chatapplication.BuildConfig
import com.example.chatapplication.R
import io.agora.rtm.*
import java.util.ArrayList

class ChatManager {
    private val TAG = ChatManager::class.java.simpleName

    private var mContext: Context? = null
    private var mRtmClient: RtmClient? = null
    private var mSendMsgOptions: SendMessageOptions? = null
    private val mListenerList: MutableList<RtmClientListener> = ArrayList<RtmClientListener>()
    private val mMessagePool: RtmMessagePool = RtmMessagePool()

    fun init() {
        val appID = mContext!!.getString(R.string.agora_app_id)
        try {
            mRtmClient = RtmClient.createInstance(mContext, appID, object : RtmClientListener {
                override fun onConnectionStateChanged(state: Int, reason: Int) {
                    for (listener in mListenerList) {
                        Log.d("chat_test", " Connection $state. Reason $reason")
                        listener.onConnectionStateChanged(state, reason)
                    }
                }

                override fun onMessageReceived(rtmMessage: RtmMessage?, peerId: String?) {
                    if (mListenerList.isEmpty()) {
                        // If currently there is no callback to handle this
                        // message, this message is unread yet. Here we also
                        // take it as an offline message.
                        mMessagePool.insertOfflineMessage(rtmMessage, peerId)
                    } else {
                        for (listener in mListenerList) {
                            listener.onMessageReceived(rtmMessage, peerId)
                        }
                    }
                }

                override fun onImageMessageReceivedFromPeer(
                    rtmImageMessage: RtmImageMessage?,
                    peerId: String?
                ) {
                    if (mListenerList.isEmpty()) {
                        // If currently there is no callback to handle this
                        // message, this message is unread yet. Here we also
                        // take it as an offline message.
                        mMessagePool.insertOfflineMessage(rtmImageMessage, peerId)
                    } else {
                        for (listener in mListenerList) {
                            listener.onImageMessageReceivedFromPeer(rtmImageMessage, peerId)
                        }
                    }
                }

                override fun onFileMessageReceivedFromPeer(rtmFileMessage: RtmFileMessage?, s: String?) {}
                override fun onMediaUploadingProgress(
                    rtmMediaOperationProgress: RtmMediaOperationProgress?,
                    l: Long
                ) {
                }

                override fun onMediaDownloadingProgress(
                    rtmMediaOperationProgress: RtmMediaOperationProgress?,
                    l: Long
                ) {
                }

                override fun onTokenExpired() {}
                override fun onPeersOnlineStatusChanged(status: Map<String?, Int?>?) {}
            })
            if (BuildConfig.DEBUG) {
                mRtmClient?.setParameters("{\"rtm.log_filter\": 65535}")
            }
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            throw RuntimeException(
                """
                NEED TO check rtm sdk init fatal error
                ${Log.getStackTraceString(e)}
                """.trimIndent()
            )
        }

        // Global option, mainly used to determine whether
        // to support offline messages now.
        mSendMsgOptions = SendMessageOptions()
    }

    fun getRtmClient(): RtmClient? {
        return mRtmClient
    }

    fun registerListener(listener: RtmClientListener) {
        mListenerList.add(listener)
    }

    fun unregisterListener(listener: RtmClientListener) {
        mListenerList.remove(listener)
    }

    fun enableOfflineMessage(enabled: Boolean) {
        mSendMsgOptions?.enableOfflineMessaging = enabled
    }

    fun isOfflineMessageEnabled(): Boolean? {
        return mSendMsgOptions?.enableOfflineMessaging
    }

    fun getSendMessageOptions(): SendMessageOptions? {
        return mSendMsgOptions
    }

    fun getAllOfflineMessages(peerId: String?): List<RtmMessage?>? {
        return mMessagePool.getAllOfflineMessages(peerId ?: "")
    }

    fun removeAllOfflineMessages(peerId: String?) {
        mMessagePool.removeAllOfflineMessages(peerId ?: "")
    }
}