// MessagesResponseTest.kt

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
    private val messagesAdapter = moshi.adapter(MessagesResponse::class.java)

    @Test
    fun `test parsing messages response`() {
        val messagesResponseJson = loadJsonResource("messages_response_sample.json")
        assertNotNull("Could not load sample JSON", messagesResponseJson)

        val messagesResponse = messagesAdapter.fromJson(messagesResponseJson!!)
        assertNotNull(messagesResponse)
        assertEquals(7, messagesResponse?.data?.size)

        // Assuming we want to test the latest assistant message
        val assistantMessages = messagesResponse?.data?.filter { it.role == "assistant" }
        assertNotNull(assistantMessages)
        assertTrue(
            "There should be at least one assistant message",
            assistantMessages!!.isNotEmpty(),
        )

        val latestAssistantMessage = assistantMessages.last()
        val contentBlock = latestAssistantMessage.content.first() as? ResponseContentBlock.TextBlock
        assertNotNull(contentBlock)
        val assistantText = contentBlock?.text?.value?.trim()
        assertNotNull(assistantText)

        // Extract JSON from assistantText
        val jsonString = extractJsonFromText(assistantText!!)
        assertNotNull(jsonString)

        val jsonReader = JsonReader.of(Buffer().writeUtf8(jsonString!!))
        val llmResponse = parseLLMResponse(jsonReader)
        assertNotNull(llmResponse)
        assertEquals("Sure, let me open the photo for you.", llmResponse.spoken)
        assertEquals(1, llmResponse.actions.size)
        val action = llmResponse.actions[0]
        assertTrue(action is Action.TapAction)
        val tapAction = action as Action.TapAction
        assertEquals(298f, tapAction.x)
        assertEquals(462f, tapAction.y)
    }

    private fun extractJsonFromText(text: String): String? {
        val jsonRegex = Regex("""\{.*\}""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = jsonRegex.find(text)
        return matchResult?.value
    }

    private fun parseLLMResponse(jsonReader: JsonReader): LLMResponse {
        var spoken: String? = null
        val actions = mutableListOf<Action>()
        var requiresFurtherAction = false
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

    // Utility function to load JSON from resource files
    private fun loadJsonResource(filename: String): String? {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream(filename)
        if (inputStream != null) {
            val source = inputStream.source().buffer()
            return source.readUtf8()
        }
        return null
    }
}
