package com.example.uitils

import com.example.chatapplication.R
import com.example.models.MessageBean
import com.example.models.MessageListBean
import io.agora.rtm.RtmMessage
import java.util.*



class MessageUtils {
    var RANDOM = Random()

    val COLOR_ARRAY = intArrayOf(
        R.drawable.sender,
        R.drawable.reciver,
        /*R.drawable.shape_circle_pink,
        R.drawable.shape_circle_pink_dark,
        R.drawable.shape_circle_yellow,
        R.drawable.shape_circle_red*/
    )

    companion object {
        const val MAX_INPUT_NAME_LENGTH = 64
        const val ACTIVITY_RESULT_CONN_ABORTED = 1
        const val INTENT_EXTRA_IS_PEER_MODE = "chatMode"
        const val INTENT_EXTRA_USER_ID = "userId"
        const val INTENT_EXTRA_TARGET_NAME = "targetName"

        private val messageListBeanList: MutableList<MessageListBean> = ArrayList<MessageListBean>()
        fun addMessageListBeanList(messageListBean: MessageListBean) {
            messageListBeanList.add(messageListBean)
        }

        // clean up list on logout
        fun cleanMessageListBeanList() {
            messageListBeanList.clear()
        }

        fun getExistMessageListBean(accountOther: String): MessageListBean? {
            val ret = existMessageListBean(accountOther)
            return if (ret > -1) {
                messageListBeanList.removeAt(ret)
            } else null
        }

        // return existing list position
        private fun existMessageListBean(userId: String): Int {
            val size = messageListBeanList.size
            for (i in 0 until size) {
                if (messageListBeanList[i].getAccountOther().equals(userId)) {
                    return i
                }
            }
            return -1
        }

        fun addMessageBean(account: String, msg: RtmMessage?) {
            val messageBean = MessageBean().setMessageBean(account, msg, false)
            val ret = existMessageListBean(account)
            if (ret == -1) {
                // account not exist new messagelistbean
                //messageBean.setBackground(COLOR_ARRAY[RANDOM.nextInt(COLOR_ARRAY.size)])
                val messageBeanList: MutableList<MessageBean> = ArrayList<MessageBean>()
                messageBeanList.add(messageBean)
                messageListBeanList.add(MessageListBean(account, messageBeanList))
            } else {
                // account exist get messagelistbean
                val bean: MessageListBean = messageListBeanList.removeAt(ret)
                val messageBeanList: MutableList<MessageBean> = bean.getMessageBeanList()?.toMutableList()!!
                /*if (messageBeanList.size > 0) {
                    messageBean.setBackground(messageBeanList[0].getBackground())
                } else {
                    messageBean.setBackground(COLOR_ARRAY[RANDOM.nextInt(COLOR_ARRAY.size)])
                }*/
                messageBeanList.add(messageBean)
                bean.setMessageBeanList(messageBeanList)
                messageListBeanList.add(bean)
            }
        }
    }


}