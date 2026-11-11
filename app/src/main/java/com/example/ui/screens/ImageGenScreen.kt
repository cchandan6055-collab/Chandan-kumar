package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiRepository
import com.example.data.ai.ArtStyle
import com.example.data.ai.ImageRatio
import com.example.ui.components.NeonGlowingCard
import com.example.ui.theme.CyberBlackBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderNeon
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberVioletAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.NeonPinkSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.OutputStream

@Composable
fun ImageGenScreen(
    aiRepository: AiRepository,
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var promptText by remember { mutableStateOf("") }
    var negativePromptText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(ArtStyle.CYBERPUNK) }
    var selectedRatio by remember { mutableStateOf(ImageRatio.RATIO_1_1) }

    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancingPrompt by remember { mutableStateOf(false) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val samplePrompts = if (language == "hi") {
        listOf(
            "नियॉन रोशनी में भीगा एक भविष्यवादी साइबरपंक शहर",
            "काँच और क्रिस्टल से बना एक रोबोटिक बाघ",
            "अंतरिक्ष में तैरता हुआ एक रहस्यमयी नियॉन गुलाबी द्वीप",
            "भविष्य की उड़ान भरने वाली कार, बारिश की रात"
        )
    } else {
        listOf(
            "Futuristic neon-drenched cyberpunk metropolis in rain",
            "Crystalline mechanical tiger with glowing magenta neural circuits",
            "Floating neon pink cybernetic island in outer space",
            "Hyper-detailed holographic AI core orb floating above obsidian altar"
        )
    }

    fun saveImageToGallery(bitmap: Bitmap) {
        try {
            val filename = "MG_AI_${System.currentTimeMillis()}.png"
            var fos: OutputStream? = null
            var imageUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MGAI")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val image = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(
                    context,
                    if (language == "hi") "छवि गैलरी में सहेजी गई" else "Image saved to Pictures/MGAI",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("image_gen_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = true) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = NeonPinkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == "hi") "एआई छवि निर्माण (Studio)" else "AI Image Studio",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "hi") "जेमिनी नैनो बनाना 2.1 द्वारा संचालित" else "Powered by Gemini Nano Banana 2.1",
                            color = NeonPinkSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Generated Image Display (if any)
        if (generatedBitmap != null) {
            NeonGlowingCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = true
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = generatedBitmap!!.asImageBitmap(),
                        contentDescription = "Generated Artwork",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonPinkPrimary)
                                .clickable { saveImageToGallery(generatedBitmap!!) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("save_image_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Save",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == "hi") "गैलरी में सहेजें" else "Save to Gallery",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading Indicator Box
        if (isGenerating) {
            NeonGlowingCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = NeonPinkPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (language == "hi") "छवि बनाई जा रही है (30-60 सेकंड लग सकते हैं)..." else "Synthesizing image with Nano Banana 2.1...",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == "hi") "कृपया प्रतीक्षा करें, दृश्य संरचना की जा रही है" else "Composing neural textures, lighting, and aspect geometry",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Error message banner
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRed.copy(alpha = 0.15f))
                    .border(1.dp, ErrorRed, RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .testTag("image_gen_error_banner")
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == "hi") "छवि निर्माण त्रुटि" else "Generation Notice",
                            color = Color(0xFFFFB4AB),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage!!,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Prompt Input
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == "hi") "छवि प्रॉम्प्ट (Prompt)" else "Creative Prompt",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Prompt Enhancer Button (uses Gemini 3.8 Flash)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPinkContainer)
                        .border(1.dp, NeonPinkPrimary, RoundedCornerShape(12.dp))
                        .clickable(enabled = promptText.isNotBlank() && !isEnhancingPrompt) {
                            isEnhancingPrompt = true
                            scope.launch {
                                val enhanced = aiRepository.enhanceImagePrompt(promptText, selectedStyle)
                                promptText = enhanced
                                isEnhancingPrompt = false
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("enhance_prompt_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Enhance",
                            tint = NeonPinkPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnhancingPrompt) "..." else (if (language == "hi") "एआई विस्तार" else "AI Enhance"),
                            color = NeonPinkLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                placeholder = {
                    Text(
                        text = if (language == "hi") "वर्णन करें कि आप क्या बनाना चाहते हैं..." else "Describe what you want to visualize in vivid detail...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPinkPrimary,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonPinkPrimary,
                    focusedContainerColor = CyberCardSurface,
                    unfocusedContainerColor = CyberCardSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("image_prompt_input"),
                minLines = 3,
                maxLines = 5
            )
        }

        // Sample Prompts Chips
        Column {
            Text(
                text = if (language == "hi") "नमूना विचार (Inspirations):" else "Quick Inspirations:",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(samplePrompts) { sample ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardElevated)
                            .border(0.5.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable { promptText = sample }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sample,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Artistic Style Selector
        Column {
            Text(
                text = if (language == "hi") "कलात्मक शैली (Artistic Style)" else "Artistic Style",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(ArtStyle.values()) { style ->
                    val isSelected = style == selectedStyle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonPinkContainer else CyberCardSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) NeonPinkPrimary else CyberBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedStyle = style }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("style_${style.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = style.displayName,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Aspect Ratio Selector
        Column {
            Text(
                text = if (language == "hi") "पहलू अनुपात (Aspect Ratio)" else "Aspect Ratio",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageRatio.values().forEach { ratio ->
                    val isSelected = ratio == selectedRatio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonPinkContainer else CyberCardSurface)
                            .border(
                                1.dp,
                                if (isSelected) NeonPinkPrimary else CyberBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedRatio = ratio }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ratio.label.substringBefore(" "),
                            color = if (isSelected) NeonPinkLight else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Negative Prompt Input
        Column {
            Text(
                text = if (language == "hi") "नकारात्मक प्रॉम्प्ट (हटाने योग्य तत्व)" else "Negative Prompt (Elements to exclude)",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == "hi") "उदाहरण: धुंधला, विकृत चेहरे, अतिरिक्त अंग, वॉटरमार्क" else "Exclude: blur, watermark, extra limbs, low resolution, bad anatomy",
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = negativePromptText,
                onValueChange = { negativePromptText = it },
                placeholder = {
                    Text(
                        text = "e.g., blurry, text, logo, oversaturated, deformed",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPinkPrimary,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonPinkPrimary,
                    focusedContainerColor = CyberCardSurface,
                    unfocusedContainerColor = CyberCardSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("negative_prompt_input"),
                maxLines = 2
            )
        }

        // Generate Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (promptText.isNotBlank() && !isGenerating)
                        Brush.horizontalGradient(listOf(NeonPinkPrimary, NeonPinkDark, CyberVioletAccent))
                    else
                        Brush.horizontalGradient(listOf(CyberCardElevated, CyberCardElevated))
                )
                .clickable(enabled = promptText.isNotBlank() && !isGenerating) {
                    isGenerating = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val bitmap = aiRepository.generateImage(
                                prompt = promptText,
                                style = selectedStyle,
                                aspectRatio = selectedRatio,
                                negativePrompt = negativePromptText
                            )
                            if (bitmap != null) {
                                generatedBitmap = bitmap
                            } else {
                                errorMessage = "Model completed request but no image candidate was returned."
                            }
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Image generation failed. Ensure your Firebase project is on the Blaze plan for Gemini image generation."
                        } finally {
                            isGenerating = false
                        }
                    }
                }
                .padding(vertical = 14.dp)
                .testTag("generate_image_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (promptText.isNotBlank()) Color.White else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGenerating) {
                        if (language == "hi") "छवि बनाई जा रही है..." else "Generating Image..."
                    } else {
                        if (language == "hi") "छवि उत्पन्न करें (Generate Image)" else "Synthesize Image with MG AI"
                    },
                    color = if (promptText.isNotBlank()) Color.White else TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
