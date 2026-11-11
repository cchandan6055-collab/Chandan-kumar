package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiRepository"

enum class ArtStyle(val displayName: String, val promptEnhancer: String) {
    CYBERPUNK("Cyberpunk Neon", "futuristic cyberpunk neon aesthetic, glowing magenta and cyan light accents, holographic elements, cinematic reflections, ultra-detailed 8k render"),
    REALISTIC("Photorealistic", "hyper-realistic professional photography, 85mm lens, natural volumetric lighting, depth of field, 8k resolution, crisp fine details"),
    ANIME_CARTOON("Anime / Cartoon", "vibrant modern anime style, clean bold line work, expressive character design, dynamic studio key art, stylized lighting"),
    ABSTRACT("Abstract Surreal", "surreal abstract art, ethereal flowing energy ribbons, vibrant dreamlike colors, geometric harmony, intricate digital artwork"),
    RENDER_3D("3D Render", "octane render 3D art, ray-traced ambient lighting, smooth polished surfaces, studio lighting, trending on ArtStation"),
    MINIMALIST("Minimalist", "clean minimalist vector art, sleek cyber aesthetic, high contrast, elegant composition, futuristic simplicity")
}

enum class ImageRatio(val label: String, val aspectRatio: AspectRatio?) {
    RATIO_1_1("1:1 Square", AspectRatio.SQUARE_1x1),
    RATIO_16_9("16:9 Wide", AspectRatio.LANDSCAPE_16x9),
    RATIO_9_16("9:16 Tall", AspectRatio.PORTRAIT_9x16),
    RATIO_4_3("4:3 Classic", AspectRatio.LANDSCAPE_4x3),
    RATIO_3_4("3:4 Portrait", AspectRatio.PORTRAIT_3x4)
}

class AiRepository {

    // Lazy initialization of Gemini 3.8 Flash for text, chat, search, and prompt crafting
    private val textModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private fun getSystemInstruction(language: String): String {
        return if (language.equals("hi", ignoreCase = true)) {
            "आप MG AI हैं — 'Intelligence That Understands'. एक अत्याधुनिक, बुद्धिमान और मित्रवत AI सहायक। " +
            "उपयोगकर्ता के प्रश्नों का उत्तर स्पष्ट, विनम्र और आकर्षक हिंदी में दें। जहाँ आवश्यक हो, मुख्य बिंदुओं को संरचित रूप में प्रस्तुत करें।"
        } else {
            "You are MG AI — 'Intelligence That Understands'. A sophisticated, highly capable, and empathetic Android AI assistant. " +
            "Provide insightful, articulate, well-structured, and helpful answers. Format with clear headings, bullet points, or concise explanations where helpful."
        }
    }

    /**
     * Streaming chat generation to minimize TTFT (Time-To-First-Token).
     */
    fun streamChat(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        language: String = "en"
    ): Flow<String> = flow {
        val sysPrompt = getSystemInstruction(language)
        val fullPrompt = if (history.isEmpty()) {
            "$sysPrompt\n\nUser Question:\n$prompt"
        } else {
            val historyContext = history.takeLast(6).joinToString("\n") { (role, msg) ->
                if (role == "user") "User: $msg" else "MG AI: $msg"
            }
            "$sysPrompt\n\nConversation Context:\n$historyContext\n\nCurrent User Question:\n$prompt"
        }

        withTimeout(60_000L) {
            textModel.generateContentStream(fullPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    emit(text)
                }
            }
        }
    }

    /**
     * Unary response generation for quick Q&A or voice assistant replies.
     */
    suspend fun generateAnswer(
        prompt: String,
        language: String = "en"
    ): String {
        val sysPrompt = getSystemInstruction(language)
        val fullPrompt = "$sysPrompt\n\nUser Question:\n$prompt"

        return withTimeout(60_000L) {
            val response = textModel.generateContent(fullPrompt)
            response.text ?: if (language.equals("hi", ignoreCase = true)) {
                "क्षमा करें, कोई उत्तर प्राप्त नहीं हुआ।"
            } else {
                "I apologize, but I could not formulate a response."
            }
        }
    }

    /**
     * Smart search engine: returns in-depth synthesized knowledge breakdown.
     */
    suspend fun executeSmartSearch(
        query: String,
        language: String = "en"
    ): String {
        val langInstruction = if (language.equals("hi", ignoreCase = true)) {
            "कृपया संपूर्ण उत्तर हिंदी में दें।"
        } else {
            "Respond in clear, articulate English."
        }

        val prompt = """
            Act as MG AI Smart Search Engine.
            Topic to investigate: "$query"
            Language instruction: $langInstruction
            
            Structure the response strictly in these sections:
            ## 🎯 Direct Summary
            (1-2 clear, direct summary paragraphs answering the query)
            
            ## 🔍 Key Insights & Analysis
            (3-5 bullet points with core concepts, data, or critical facts)
            
            ## 💡 Practical Implications / Actionable Advice
            (How this applies in practice, tips, or takeaways)
            
            ## 🌐 Explore Further
            (3 related follow-up search queries to explore)
        """.trimIndent()

        return withTimeout(60_000L) {
            val response = textModel.generateContent(prompt)
            response.text ?: "No search results available."
        }
    }

    /**
     * Prompt Expander: Takes a brief idea and transforms it into an optimized creative image prompt.
     */
    suspend fun enhanceImagePrompt(rawIdea: String, style: ArtStyle): String {
        val prompt = """
            You are a world-class AI visual prompt engineer for MG AI.
            The user wants to generate an image based on: "$rawIdea"
            Style preference: ${style.displayName}
            
            Write a single, highly detailed, evocative visual prompt (under 60 words) describing lighting, textures, composition, and mood.
            Output ONLY the improved prompt text. No markdown, no quotes, no conversational filler.
        """.trimIndent()

        return try {
            withTimeout(30_000L) {
                val response = textModel.generateContent(prompt)
                response.text?.trim() ?: rawIdea
            }
        } catch (e: Exception) {
            Log.w(TAG, "Prompt enhancement failed, using raw prompt", e)
            rawIdea
        }
    }

    /**
     * Generate an AI image using Firebase AI Logic Nano Banana model (`gemini-nano-banana-2.1`).
     */
    suspend fun generateImage(
        prompt: String,
        style: ArtStyle = ArtStyle.CYBERPUNK,
        aspectRatio: ImageRatio = ImageRatio.RATIO_1_1,
        negativePrompt: String = ""
    ): Bitmap? {
        val composedPrompt = buildString {
            append(prompt.trim())
            append(". Style: ").append(style.promptEnhancer)
            if (negativePrompt.isNotBlank()) {
                append(". Negative constraints (exclude): ").append(negativePrompt.trim())
            } else {
                append(". Avoid: blurry, low resolution, artifacts, distorted proportions.")
            }
        }

        Log.d(TAG, "Requesting AI image generation with prompt: $composedPrompt")

        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
            val builder = ImageConfig.builder().setImageSize(ImageSize.SIZE_1K)
            aspectRatio.aspectRatio?.let { ratio ->
                builder.setAspectRatio(ratio)
            }
            imageConfig = builder.build()
        }

        val imageModel = Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = config
        )

        return withTimeout(60_000L) {
            val response = imageModel.generateContent(composedPrompt)
            val imagePart = response.candidates.firstOrNull()
                ?.content?.parts?.filterIsInstance<ImagePart>()
                ?.firstOrNull()

            imagePart?.image
        }
    }

    /**
     * Notes AI: Summarize or elaborate notes.
     */
    suspend fun processNoteWithAi(content: String, action: String, language: String): String {
        val instruction = if (action == "summarize") {
            "Summarize the following note into concise bullet points and a 1-sentence TL;DR:"
        } else {
            "Elaborate and organize the following note with ideas, structure, and actionable steps:"
        }

        val langSuffix = if (language.equals("hi", ignoreCase = true)) "Provide response in Hindi." else "Provide response in English."
        val prompt = "$instruction\n\n$content\n\n$langSuffix"

        return withTimeout(45_000L) {
            val response = textModel.generateContent(prompt)
            response.text ?: content
        }
    }
}
