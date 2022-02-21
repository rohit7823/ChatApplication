package com.example.chatapplication.rtm

import io.agora.rtm.RtmMessage
import java.util.ArrayList
import java.util.HashMap

/**
 * Receives and manages messages from RTM engine.
 */
class RtmMessagePool {
    private val mOfflineMessageMap: MutableMap<String, MutableList<RtmMessage>?> =
        HashMap<String, MutableList<RtmMessage>?>()

    fun insertOfflineMessage(rtmMessage: RtmMessage?, peerId: String?) {
        val contains = mOfflineMessageMap.containsKey(peerId)
        val list: MutableList<RtmMessage>? =
            if (contains) mOfflineMessageMap[peerId] else ArrayList<RtmMessage>()
        rtmMessage?.let {
            list?.add(rtmMessage)
        }
        if (!contains) {
            peerId?.let {
                mOfflineMessageMap[peerId] = list
            }
        }
    }

    fun getAllOfflineMessages(peerId: String): List<RtmMessage>? {
        return if (mOfflineMessageMap.containsKey(peerId)) mOfflineMessageMap[peerId] else ArrayList<RtmMessage>()
    }

    fun removeAllOfflineMessages(peerId: String) {
        mOfflineMessageMap.remove(peerId)
    }
}
