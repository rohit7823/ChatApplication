package com.example.chatapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatapplication.app.ChatApp
import com.example.chatapplication.rtm.ChatManager
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient

const val TOKEN =
    "006d68d248ed2544b15856e8398edd4e0d2IACjrAT6dpwDbODGYW2sqUn0/Ay2Dr7v/75EsRHt2hzzudW2h6MAAAAAEAC+aHo313AUYgEA6APXcBRi"

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private val chatManager by lazy { ChatApp().the()?.getChatManager() }
    private val mRtmClient by lazy { chatManager?.getRtmClient() }
    private val mUserId = "rohit"
    private var mIsInChat = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doLogin()
    }

    private fun doLogin() {
        mIsInChat = true
        mRtmClient?.login(TOKEN, mUserId, object : ResultCallback<Void?> {
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
                    "User: $mUserId failed to log in to the RTM system!$errorInfo"
                val duration = Toast.LENGTH_SHORT
                Log.d("", text.toString())
                runOnUiThread {
                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.show()
                }
            }
        })
    }
}

@Composable
fun ThisActivityContent(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chat") {
        composable(route = "chat") {
        }
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatApplicationTheme {
        Greeting("Android")
    }
}