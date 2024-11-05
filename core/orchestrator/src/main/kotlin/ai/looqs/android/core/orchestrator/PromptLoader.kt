package ai.looqs.android.core.orchestrator

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object PromptLoader {

    fun loadPrompt(context: Context): String {
        val assetManager = context.assets
        val inputStream = assetManager.open("assistant_prompt.md")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val prompt = bufferedReader.use { it.readText() }
        return prompt
    }
}