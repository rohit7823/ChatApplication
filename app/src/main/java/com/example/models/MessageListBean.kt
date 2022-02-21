package com.example.models

import com.example.chatapplication.rtm.ChatManager
import io.agora.rtm.RtmMessage

class MessageListBean {
    private var accountOther: String? = null
    private var messageBeanList: MutableList<MessageBean>? = null

    constructor(account: String?, messageBeanList: MutableList<MessageBean>?) {
        accountOther = account
        this.messageBeanList = messageBeanList
    }

    /**
     * Create message list bean from offline messages
     *
     * @param account     peer user id to find offline messages from
     * @param chatManager chat manager that managers offline message pool
     */
    constructor(account: String?, chatManager: ChatManager) {
        accountOther = account
        messageBeanList = mutableListOf<MessageBean>()
        chatManager.getAllOfflineMessages(account)?.apply {
            val messageList: List<RtmMessage?> = this
            for (m in messageList) {
                // All offline messages are from peer users
                val bean = MessageBean()
                    .setMessageBean(account, m, false)
                messageBeanList?.add(bean)
            }
        }

    }

    fun getAccountOther(): String? {
        return accountOther
    }

    fun setAccountOther(accountOther: String?) {
        this.accountOther = accountOther
    }

    fun getMessageBeanList(): List<MessageBean>? {
        return messageBeanList
    }

    fun setMessageBeanList(messageBeanList: MutableList<MessageBean>?) {
        this.messageBeanList = messageBeanList
    }
}