package com.example.models

import io.agora.rtm.RtmMessage

data class MessageBean(
    var account: String = "",
    var message: RtmMessage? = null,
    val cacheFile: String = "",
    val background: Int = 0,
    var beSelf: Boolean = false
) {
    fun setMessageBean(account: String?, message: RtmMessage?, isSelf: Boolean): MessageBean {
        return MessageBean(
            account = account ?: "",
            message = message,
            cacheFile = "",
            background = 0,
            beSelf = isSelf
        )
    }
}
