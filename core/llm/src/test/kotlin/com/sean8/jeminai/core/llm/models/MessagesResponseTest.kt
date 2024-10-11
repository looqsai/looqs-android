package com.sean8.jeminai.core.llm.models

import com.sean8.jeminai.core.actions.Action
import com.sean8.jeminai.core.llm.MoshiProvider
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import okio.buffer
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessagesResponseTest {

    private val moshiProvider = MoshiProvider()
    private val moshi: Moshi = moshiProvider.moshi

    @Test
    fun `test parsing messages response`() {
        val inputStream =
            this.javaClass.classLoader?.getResourceAsStream("messages_response_sample.json")
        assertNotNull("Could not load sample JSON", inputStream)
        val source = inputStream!!.source().buffer()
        val json = source.readUtf8()
        val messagesAdapter = moshi.adapter(MessagesResponse::class.java)
        val messagesResponse = messagesAdapter.fromJson(json)
        assertNotNull("Failed to parse MessagesResponse", messagesResponse)

        // Perform assertions on the parsed object
        messagesResponse?.let {
            assertEquals("list", it.objectType)
            assertEquals(1, it.data.size)
            assertEquals("msg_ZUS78y5kvhEAwi0VFxmuje3Z", it.firstId)
            assertEquals("msg_AdOOIXcf2hsahLwiDutj94Ao", it.lastId)
            assertEquals(false, it.hasMore)

            // Check the first message
            val firstMessage = it.data[0]
            assertEquals("msg_ZUS78y5kvhEAwi0VFxmuje3Z", firstMessage.id)
            assertEquals("thread.message", firstMessage.objectType)
            assertEquals(1728577575L, firstMessage.createdAt)
            assertEquals("asst_wMrjZoz0CyMwQmrmLitt7GV3", firstMessage.assistantId)
            assertEquals("thread_duxU78Rp0pfi4QRoirktwo4k", firstMessage.threadId)
            assertEquals("run_lGLsmGCm9Ms9dxBKvL5fGrrP", firstMessage.runId)
            assertEquals("assistant", firstMessage.role)
            assertNotNull(firstMessage.content)
            assertEquals(1, firstMessage.content.size)

            val contentBlock = firstMessage.content[0] as ResponseContentBlock.TextBlock
            val textContent = contentBlock.text
            assertNotNull(textContent)

            // Extract and clean the JSON string from textContent.value
            var jsonString = textContent.value.trim()

            // Remove any wrapping
            jsonString = jsonString
                .removeSurrounding("```")
                .removeSurrounding("\"")
                .replace("\\\"", "\"")
                .replace("\\n", "\n")

            val jsonReader = JsonReader.of(Buffer().writeUtf8(jsonString))
            val llmResponse = parseLLMResponse(jsonReader)
            assertNotNull("Failed to parse LLMResponse", llmResponse)

            llmResponse?.let { response ->
                assertTrue(response.spoken.isNotEmpty())
                // Further assertions on actions
                assertEquals(1, response.actions.size)
                val action = response.actions[0]
                assertTrue(action is Action.TapAction)
                val tapAction = action as Action.TapAction
                // Assertions on tapAction fields
                assertNotNull(tapAction.x)
                assertNotNull(tapAction.y)
            }
        }
    }

    private fun parseLLMResponse(jsonReader: JsonReader): LLMResponse {
        var spoken: String? = null
        val actions = mutableListOf<Action>()
        var requiresFurtherAction: Boolean = false
        jsonReader.isLenient = true
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "spoken" -> {
                    spoken = jsonReader.nextString()
                }

                "actions" -> {
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        try {
                            val actionAdapter = moshi.adapter(Action::class.java)
                            val action = actionAdapter.fromJson(jsonReader)
                            if (action != null) {
                                actions.add(action)
                            }
                        } catch (e: Exception) {
                            println("Failed to parse action: ${e.message}")
                            jsonReader.skipValue()
                        }
                    }
                    jsonReader.endArray()
                }

                "requiresFurtherAction" -> {
                    requiresFurtherAction = jsonReader.nextBoolean()
                }

                else -> {
                    jsonReader.skipValue()
                }
            }
        }
        jsonReader.endObject()
        if (spoken == null) {
            spoken = ""
        }
        return LLMResponse(
            spoken = spoken,
            actions = actions,
            requiresFurtherAction = requiresFurtherAction,
        )
    }
}
