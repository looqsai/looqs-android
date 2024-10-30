package com.sean8.jeminai.core.llm

object PromptConstants {
    const val ECHO_REQUEST = "ECHO"

    fun formatCommand(command: String) = "<< $command >>"
}