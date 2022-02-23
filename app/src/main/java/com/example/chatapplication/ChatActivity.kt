package com.example.chatapplication

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.afollestad.materialdialogs.MaterialDialog
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.callback.StorageAccessCallback
import com.anggrayudi.storage.file.StorageType
import com.example.chatapplication.app.ChatApp
import com.example.chatapplication.rtm.ChatManager
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import com.example.models.MessageBean
import com.example.models.MessageListBean
import com.example.uitils.MessageUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.skydoves.landscapist.rememberDrawablePainter
import io.agora.rtm.*
import kotlinx.coroutines.NonCancellable.cancel


const val TOKEN =
    "006d68d248ed2544b15856e8398edd4e0d2IACUpK4UYBIKJbMdZ/gXNXW3VO4ECP8gR6CbfXWUL2IpZ7Rc5oMAAAAAEACZ+YAUQ3wXYgEA6ANDfBdi"


class ChatActivity : ComponentActivity() {

    private val openStorage by lazy { SimpleStorage(this) }
    private lateinit var chatManager: ChatManager //by lazy { ChatManager() }
    private lateinit var rtmClient: RtmClient //by lazy { ChatManager().getRtmClient() }
    private lateinit var clientListener: RtmClientListener //by lazy { MyRtmClientListener() }
    private lateinit var messageListBean: MutableList<MessageBean> //by lazy { mutableListOf<MessageBean>() }
    private val generalMessageList = mutableStateListOf<MessageBean>()
    val mPeerId = "rajdeep"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doInitialWork()

        setContent {
            ChatApplicationTheme {
                ScreenContent(messageList = generalMessageList)
            }
        }
    }

    private fun doInitialWork() {

        ChatApp.the()?.getChatManager()?.let {
            chatManager = it
        }

        chatManager.getRtmClient()?.let {
            rtmClient = it
        }

        clientListener = MyRtmClientListener()
        messageListBean = mutableListOf()
        doLogin()

        chatManager.registerListener(clientListener)
        val chatHistory = MessageUtils.getExistMessageListBean(mPeerId)
        // load chat history records
        chatHistory?.let {
            chatHistory.getMessageBeanList()?.let {
                messageListBean.addAll(it)
                generalMessageList.addAll(it)
            }
        }


        // load offline messages since last chat with this peer.
        // Then clear cached offline messages from message pool
        // since they are already consumed.
        val offlineMessageBean = MessageListBean(mPeerId, chatManager)
        messageListBean.addAll(
            offlineMessageBean.getMessageBeanList()?.toMutableList() ?: emptyList()
        )
        generalMessageList.addAll(
            offlineMessageBean.getMessageBeanList()?.toMutableList() ?: emptyList()
        )
        chatManager.removeAllOfflineMessages(mPeerId)
    }

    private fun doLogin() {
        val mIsInChat = true
        rtmClient.login(TOKEN, "rohit", object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                /*Log.i(TAG, "login success")
                runOnUiThread {
                    val intent = Intent(this@LoginActivity, SelectionActivity::class.java)
                    intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, mUserId)
                    startActivity(intent)
                }*/
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                val text: CharSequence =
                    "User: $mPeerId failed to log in to the RTM system!$errorInfo"
                val duration = Toast.LENGTH_SHORT
                Log.d("testing", text.toString())
                runOnUiThread {
                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.show()
                }
            }
        })
    }

    inner class MyRtmClientListener : RtmClientListener {
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            runOnUiThread {
                when (state) {
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_CONNECTING -> {
                        Log.d("testing", "Connecting")
                    }
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_CONNECTED -> {
                        Log.d("testing", "Connected")

                    }
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> {
                        showToast(
                            getString(R.string.reconnecting)
                        )
                        Log.d("RtmClientListener", getString(R.string.reconnecting))
                    }
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                        showToast(getString(R.string.account_offline))
                        setResult(MessageUtils.ACTIVITY_RESULT_CONN_ABORTED)
                        finish()
                    }
                }
            }
        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {
            Log.d("RtmClientListener", "$message $peerId")
            runOnUiThread {
                if (peerId == mPeerId) {
                    val messageBean = MessageBean().setMessageBean(peerId, message, false)
                    //messageBean.setBackground(getMessageColor(peerId))
                    addToGeneralMessageList(messageBean)
                    /*mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    mRecyclerView.scrollToPosition(mMessageBeanList.size - 1)*/
                } else {
                    //MessageUtils.addMessageBean(peerId, message)
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
        override fun onPeersOnlineStatusChanged(map: Map<String, Int>) {
            Log.d("RtmClientListener", "onPeersOnlineStatusChanged")
        }
    }

    private fun addToGeneralMessageList(messageBean: MessageBean) {
        messageListBean.add(messageBean)
        generalMessageList.add(messageBean)

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
        rtmClient.sendMessageToPeer(
            mPeerId,
            message,
            chatManager.getSendMessageOptions(),
            object : ResultCallback<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    Log.d("sent_testing", "Message sent!!")
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


    @Composable
    fun ScreenContent(
        messageList: SnapshotStateList<MessageBean>,
    ) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                ToolbarContent()
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.8f),
                ) {
                    items(messageList) {
                        ColumnContent(messageBean = it)
                    }
                }
                SendMessageContent {
                    Log.d("testing", it)
                    sendMessage(it)
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun PreviewContent() {
        ChatApplicationTheme {
        }
    }


    @Composable
    fun ColumnContent(messageBean: MessageBean) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            RowContent(messageBean)
        }
    }

    @Composable
    fun RowContent(message: MessageBean) {
        if (!message.beSelf) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp, 0.dp, 0.dp, 0.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Card(modifier = Modifier.wrapContentSize()) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(Cyan),
                        text = message.message?.text ?: "",
                        textAlign = TextAlign.Justify,
                        color = Black
                    )
                }
            }

        } else {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(0.dp, 0.dp, 10.dp, 0.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Card(modifier = Modifier.wrapContentSize()) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(LightGray),
                        text = message.message?.text ?: "",
                        textAlign = TextAlign.Justify,
                        color = White
                    )
                }
            }

        }

    }


    private fun sendMessage(msg: String) {

        val newMessage = rtmClient.createMessage()
        newMessage?.text = msg

        val messageBean = MessageBean(account = mPeerId, message = newMessage, beSelf = true)
        addToGeneralMessageList(messageBean)

        newMessage?.let {
            sendPeerMessage(it)
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
                text = "Dr. Victor Le Roy",
                Modifier.wrapContentSize(),
                color = Black,
                fontSize = 25.sp
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
    fun SendMessageContent(sendMessage: (String) -> Unit) {

        var currentText by remember {
            mutableStateOf("")
        }

        var color by remember {
            mutableStateOf(Red)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = {
                    color = openFileChoosingOptions(color)
                },
                backgroundColor = color,
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(
                    painter = if (color == Red) {
                        painterResource(id = R.drawable.ic_add_24)
                    } else {
                        painterResource(id = R.drawable.ic_close)
                    },

                    contentDescription = null
                )
            }
            TextField(
                value = currentText,
                onValueChange = {
                    currentText = it
                },
                shape = RoundedCornerShape(25.dp),
                placeholder = { Text(text = "Type a message") },
                enabled = true,
                trailingIcon = {
                    Icon(
                        painterResource(id = R.drawable.ic_send_svgrepo_com),
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp)
                            .clickable { sendMessage(currentText) },
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

    private fun openFileChoosingOptions(color: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
        if (color == Red) {
            askAllStoragePermission()
            return LightGray
        }
        return Red
    }

    private fun askAllStoragePermission() {

        openStorage.storageAccessCallback = object : StorageAccessCallback {
            override fun onExpectedStorageNotSelected(
                requestCode: Int,
                selectedFolder: DocumentFile,
                selectedStorageType: StorageType,
                expectedBasePath: String,
                expectedStorageType: StorageType
            ) {

            }

            override fun onRootPathNotSelected(
                requestCode: Int,
                rootPath: String,
                uri: Uri,
                selectedStorageType: StorageType,
                expectedStorageType: StorageType
            ) {
                MaterialDialog(this@ChatActivity)
                    .message(text = "Please select $rootPath")
                    .negativeButton(R.string.cancel)
                    .positiveButton {
                        val initialRoot = if(expectedStorageType.isExpected(selectedStorageType)) {
                            selectedStorageType
                        }else expectedStorageType
                        openStorage.requestStorageAccess(openStorage.requestCodeStorageAccess, initialRoot, expectedStorageType)
                    }.show()

            }

            override fun onRootPathPermissionGranted(requestCode: Int, root: DocumentFile) {
                TODO("Not yet implemented")
            }

            override fun onStoragePermissionDenied(requestCode: Int) {
                TODO("Not yet implemented")
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        openStorage.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        openStorage.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}


