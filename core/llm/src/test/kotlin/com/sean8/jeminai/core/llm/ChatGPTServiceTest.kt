// ChatGPTServiceTest.kt

package com.sean8.jeminai.core.llm

import com.sean8.jeminai.core.actions.Action
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ChatGPTServiceTest {

    private lateinit var chatGPTService: ChatGPTService
    private lateinit var mockWebServer: MockWebServer
    private val moshi = MoshiProvider().moshi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        // Use the mockWebServer's URL in the OkHttpClient
        val client = OkHttpClient.Builder()
            .build()

        // Modify the ChatGPTService to accept an OkHttpClient and apiUrl for testing
        chatGPTService = ChatGPTService(MoshiProvider(), client, baseUrl.toString())
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test initializeAssistant`() = runBlocking {
        // Load the assistant response JSON from external file
        val assistantResponseJson = loadJsonResource("assistant_response.json")
        assertNotNull("Assistant response JSON should not be null", assistantResponseJson)

        mockWebServer.enqueue(MockResponse().setBody(assistantResponseJson).setResponseCode(200))

        val assistant = chatGPTService.initializeAssistant()
        assertNotNull(assistant)
        assertEquals("asst_test123", assistant.id)
    }

    @Test
    fun `test createThread`() = runBlocking {
        // Load the thread response JSON from external file
        val threadResponseJson = loadJsonResource("thread_response.json")
        assertNotNull("Thread response JSON should not be null", threadResponseJson)

        mockWebServer.enqueue(MockResponse().setBody(threadResponseJson).setResponseCode(200))

        val thread = chatGPTService.createThread()
        assertNotNull(thread)
        assertEquals("thread_test123", thread.id)
    }

    @Test
    fun `test sendMessage and parseAssistantReply`() = runBlocking {
        // Initialize assistant
        val assistantResponseJson = loadJsonResource("assistant_response.json")
        mockWebServer.enqueue(MockResponse().setBody(assistantResponseJson).setResponseCode(200))
        val assistant = chatGPTService.initializeAssistant()
        assertNotNull(assistant)

        // Mock adding message to thread (empty response)
        mockWebServer.enqueue(MockResponse().setBody("{}").setResponseCode(200))

        // Mock running assistant
        val runResponseJson = loadJsonResource("run_response.json")
        mockWebServer.enqueue(MockResponse().setBody(runResponseJson).setResponseCode(200))

        // Mock polling run status (simulate the run reaching 'completed' status)
        val runStatusResponseJson = loadJsonResource("run_status_completed.json")
        // Enqueue multiple times if your code polls multiple times
        mockWebServer.enqueue(MockResponse().setBody(runStatusResponseJson).setResponseCode(200))

        // Mock fetching messages
        val messagesResponseJson = loadJsonResource("messages_response_sample.json")
        mockWebServer.enqueue(MockResponse().setBody(messagesResponseJson).setResponseCode(200))

        val llmResponse = chatGPTService.sendMessage("thread_test123", "Open email", null)
        assertNotNull(llmResponse)
        assertEquals("Absolutely, I will try to open 'From Now'.", llmResponse?.spoken)
        assertEquals(1, llmResponse?.actions?.size)
        val action = llmResponse?.actions?.first() as Action.TapAction
        assertEquals(180f, action.x)
        assertEquals(300f, action.y)
    }

    // Utility function to load JSON from resource files
    private fun loadJsonResource(filename: String): String {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream(filename)
        if (inputStream != null) {
            val source = inputStream.source().buffer()
            return source.readUtf8()
        } else {
            throw IllegalStateException("Must load JSON from test resource")
        }
    }
}
