package com.example.chatapplication

import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatapplication.rtm.ChatManager
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import com.example.models.MessageBean
import com.example.uitils.MessageUtils
import io.agora.rtm.*
import org.intellij.lang.annotations.JdkConstants

class ChatFragment : ComponentActivity() {

    private val chatManager by lazy { ChatManager() }
    private val rtmClient by lazy { ChatManager().getRtmClient() }
    private val clientListener by lazy { MyRtmClientListener() }
    private val messageListBean by lazy { mutableListOf<MessageBean>() }
    private val mPeerId = "rohit"
    private var userMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatApplicationTheme {
                ScreenContent(messageList = messageListBean, userMessage = userMessage)
            }
        }
    }

    inner class MyRtmClientListener : RtmClientListener {
        override fun onConnectionStateChanged(state: Int, reason: Int) {

            runOnUiThread {
                when (state) {
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> showToast(
                        getString(R.string.reconnecting)
                    )
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                        showToast(getString(R.string.account_offline))
                        setResult(MessageUtils.ACTIVITY_RESULT_CONN_ABORTED)
                        finish()
                    }
                }
            }
        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {
            runOnUiThread {
                if (peerId == mPeerId) {
                    val messageBean = MessageBean().setMessageBean(peerId, message, false)
                    //messageBean.setBackground(getMessageColor(peerId))
                    messageListBean.add(messageBean)
                    /*mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    mRecyclerView.scrollToPosition(mMessageBeanList.size - 1)*/
                } else {
                    MessageUtils.addMessageBean(peerId, message)
                }
            }
        }

        override fun onImageMessageReceivedFromPeer(
            rtmImageMessage: RtmImageMessage,
            peerId: String
        ) {
            /*runOnUiThread(Runnable {
                if (peerId == mPeerId) {
                    val messageBean = MessageBean(peerId, rtmImageMessage, false)
                    messageBean.setBackground(getMessageColor(peerId))
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    mRecyclerView.scrollToPosition(mMessageBeanList.size - 1)
                } else {
                    MessageUtil.addMessageBean(peerId, rtmImageMessage)
                }
            })*/
        }

        override fun onFileMessageReceivedFromPeer(rtmFileMessage: RtmFileMessage, s: String) {}
        override fun onMediaUploadingProgress(
            rtmMediaOperationProgress: RtmMediaOperationProgress,
            l: Long
        ) {
        }

        override fun onMediaDownloadingProgress(
            rtmMediaOperationProgress: RtmMediaOperationProgress,
            l: Long
        ) {
        }

        override fun onTokenExpired() {}
        override fun onPeersOnlineStatusChanged(map: Map<String, Int>) {}
    }


    private fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun sendPeerMessage(message: RtmMessage) {
        rtmClient?.sendMessageToPeer(
            mPeerId,
            message,
            chatManager.getSendMessageOptions(),
            object : ResultCallback<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    // do nothing
                }

                override fun onFailure(errorInfo: ErrorInfo) {
                    // refer to RtmStatusCode.PeerMessageState for the message state
                    val errorCode = errorInfo.errorCode
                    runOnUiThread {
                        when (errorCode) {
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_TIMEOUT, RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_FAILURE -> showToast(
                                getString(R.string.send_msg_failed)
                            )
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_PEER_UNREACHABLE -> {
                                Log.d("bug_testing", errorInfo.errorDescription)
                                showToast(getString(R.string.peer_offline))
                            }
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_CACHED_BY_SERVER -> showToast(
                                getString(R.string.message_cached)
                            )
                        }
                    }
                }
            })
    }


    @Preview(showBackground = true)
    @Composable
    fun PreviewContent() {
        ChatApplicationTheme {
            ScreenContent(messageList = messageListBean, userMessage = userMessage)
        }
    }
}


@Composable
fun ColumnContent(messageBean: MessageBean) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        RowContent(messageBean)
    }
}

@Composable
fun RowContent(message: MessageBean) {
    Surface {
        if (!message.beSelf) {
            Column(Modifier.wrapContentWidth(), horizontalAlignment = Alignment.Start) {
                Image(
                    painter = painterResource(id = R.drawable.reciver),
                    null,
                    modifier = Modifier.wrapContentSize()
                )
                Text(
                    text = message.message?.text ?: "",
                    textAlign = TextAlign.Justify,
                    color = Black
                )
            }
        } else {
            Column(Modifier.wrapContentWidth(), horizontalAlignment = Alignment.End) {
                Image(
                    painter = painterResource(id = R.drawable.sender),
                    null,
                    modifier = Modifier.wrapContentSize()
                )
                Text(
                    text = message.message?.text ?: "",
                    textAlign = TextAlign.Justify,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}


@Composable
fun ScreenContent(messageList: MutableList<MessageBean>, userMessage: String) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ToolbarContent()
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                items(messageList) {
                    ColumnContent(messageBean = it)
                }
            }
            SendMessageContent(userMessage)
        }
    }
}

@Composable
fun ToolbarContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .absolutePadding(0.dp, 16.dp, 0.dp, 0.dp)
            .background(White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_back_ios_new_24),
            contentDescription = null,
            Modifier
                .height(30.dp)
                .width(30.dp)
        )
        Text(
            text = "Dr. Victor Le Roy", Modifier.wrapContentSize(), color = Black, fontSize = 25.sp
        )
        Image(
            painter = painterResource(id = R.drawable.dummy_doc),
            contentDescription = null,
            Modifier
                .height(50.dp)
                .width(50.dp)
        )
    }
}

@Composable
fun SendMessageContent(userMessage: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        FloatingActionButton(
            onClick = { },
            backgroundColor = androidx.compose.ui.graphics.Color.Red,
            modifier = Modifier.wrapContentSize()
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_add_24), contentDescription = null)
        }
        TextField(
            value = userMessage,
            onValueChange = {},
            shape = RoundedCornerShape(25.dp),
            placeholder = { Text(text = "Type a message") },
            trailingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_send_svgrepo_com),
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp),
                    contentDescription = null,
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                textColor = androidx.compose.ui.graphics.Color.Gray,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedLabelColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            ),

        )
    }
}

