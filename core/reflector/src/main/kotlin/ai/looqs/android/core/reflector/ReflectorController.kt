package ai.looqs.android.core.reflector

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReflectorController @Inject constructor() {

    private val TAG = "ReflectorController"
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val messageAdapter = moshi.adapter(ReflectorMessage::class.java)

    // Modify the client initialization
    private val client = OkHttpClient.Builder()
        .pingInterval(30, java.util.concurrent.TimeUnit.SECONDS) // Send a ping every 30 seconds
        .build()

    private var webSocket: WebSocket? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<ReflectorMessage>(replay = 0)
    val incomingMessages: SharedFlow<ReflectorMessage> get() = _incomingMessages.asSharedFlow()

    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect(url: String) {
        if (_isConnected.value) {
            Log.d(TAG, "Already connected to WebSocket.")
            return
        }

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal Closure")
        _isConnected.value = false
    }

    fun sendMessage(message: ReflectorMessage) {
        val json = messageAdapter.toJson(message)
        webSocket?.send(json)
    }

    private fun createWebSocketListener() = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected")
            _isConnected.value = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.v(TAG, "Received message: $text")
            val message = messageAdapter.fromJson(text)
            message?.let {
                scope.launch {
                    _incomingMessages.emit(it)
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // Handle binary messages if needed
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code / $reason")
            _isConnected.value = false
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code / $reason")
            _isConnected.value = false
            attemptReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
            _isConnected.value = false
            attemptReconnect()
        }
    }

    private fun attemptReconnect() {
        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            Log.d(TAG, "Attempting to reconnect... ($reconnectAttempts/$maxReconnectAttempts)")
            scope.launch {
                delay(2000L * reconnectAttempts) // Exponential backoff
                connect(webSocket?.request()?.url.toString())
            }
        } else {
            Log.e(TAG, "Max reconnect attempts reached. Giving up.")
        }
    }
}
