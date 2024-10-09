package com.sean8.jeminai.core.llm

interface TextToSpeechService {
    suspend fun speak(text: String)
}
