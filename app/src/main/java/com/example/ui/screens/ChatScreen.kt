package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.AvatarState
import com.example.ui.CampusViewModel
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.CyberChip
import com.example.ui.components.HologramAvatarView
import com.example.ui.theme.AiBubble
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UserBubble
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val avatarState by viewModel.avatarState.collectAsState()
    val isVoiceListening by viewModel.isVoiceListening.collectAsState()
    val isSpeakingAudio by viewModel.isSpeakingAudio.collectAsState()
    val voiceStatusMessage by viewModel.voiceStatusMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAvatar by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    // Speech Intent Fallback Launcher
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()?.trim()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onVoiceSpeechResult(spokenText)
            }
        }
    }

    // Launch system speech recognition intent dialog
    fun launchSpeechIntent() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask CampusAI anything about the college...")
        }
        try {
            speechIntentLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Voice recognition service is not available", Toast.LENGTH_SHORT).show()
        }
    }

    // Accompanist Permissions for Microphone
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO) { isGranted ->
        if (isGranted) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                viewModel.startVoiceRecognition(context)
            } else {
                launchSpeechIntent()
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice queries", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleMicClick() {
        if (isVoiceListening) {
            viewModel.stopVoiceRecognition()
        } else {
            if (micPermissionState.status.isGranted) {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    viewModel.startVoiceRecognition(context)
                } else {
                    launchSpeechIntent()
                }
            } else if (micPermissionState.status.shouldShowRationale) {
                showPermissionRationaleDialog = true
            } else {
                micPermissionState.launchPermissionRequest()
            }
        }
    }

    val listState = rememberLazyListState()

    // Auto-scroll on new messages
    LaunchedEffect(chatMessages.size, isProcessing) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val filteredMessages = remember(chatMessages, searchQuery) {
        if (searchQuery.isBlank()) {
            chatMessages
        } else {
            chatMessages.filter { it.text.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Microphone Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = {
                Text(
                    text = "Microphone Permission Required",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "CampusAI uses your microphone to transcribe your voice inquiries about college admissions, courses, fees, placements, and facilities hands-free.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                        micPermissionState.launchPermissionRequest()
                    }
                ) {
                    Text("Allow Access", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Not Now", color = TextMuted)
                }
            },
            containerColor = CyberCardDark
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .imePadding()
    ) {
        // Chat Header Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceDark)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isVoiceListening) CyberPink else NeonCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CampusAI 3D Assistant",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (avatarState) {
                            AvatarState.LISTENING -> "Listening to audio..."
                            AvatarState.THINKING -> "RAG Engine thinking..."
                            AvatarState.SPEAKING -> "Synthesizing voice response..."
                            AvatarState.IDLE -> "Online • College Knowledge Base Connected"
                        },
                        color = when (avatarState) {
                            AvatarState.LISTENING -> CyberPink
                            AvatarState.THINKING -> NeonCyanLight
                            AvatarState.SPEAKING -> CyberEmerald
                            AvatarState.IDLE -> TextMuted
                        },
                        fontSize = 11.sp
                    )
                }
            }

            Row {
                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search messages",
                        tint = NeonCyan
                    )
                }
                IconButton(onClick = { showAvatar = !showAvatar }) {
                    Icon(
                        imageVector = if (showAvatar) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle 3D Avatar",
                        tint = NeonCyan
                    )
                }
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Search Bar in Chat
        AnimatedVisibility(visible = isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceDark)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search in conversation...", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder
                    )
                )
            }
        }

        // 3D Holographic Avatar Section (Collapsible)
        AnimatedVisibility(visible = showAvatar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                HologramAvatarView(
                    avatarState = avatarState,
                    height = 180.dp
                )
            }
        }

        // Voice Waveform Indicator
        if (isVoiceListening || isSpeakingAudio) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCardDark.copy(alpha = 0.5f))
                    .padding(vertical = 4.dp)
            ) {
                AudioWaveformVisualizer(
                    isActive = true,
                    isSpeaking = isSpeakingAudio,
                    color = if (isSpeakingAudio) CyberEmerald else CyberPink
                )
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickPrompts = listOf(
                "What courses are available?",
                "What is the admission process?",
                "Show today's notices",
                "Tell me about the examination schedule",
                "What are the college fees?",
                "Tell me about placements",
                "Where is the library?",
                "What are the hostel facilities?",
                "What is my attendance percentage?"
            )
            items(quickPrompts) { prompt ->
                CyberChip(
                    text = prompt,
                    onClick = { viewModel.sendChatMessage(prompt) }
                )
            }
        }

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filteredMessages) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("CampusAI Message", msg.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSpeak = { viewModel.speakText(msg.text) }
                )
            }

            // AI Thinking / Typing Indicator
            if (isProcessing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AiBubble)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CampusAI is retrieving knowledge...",
                                color = NeonCyanLight,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Realtime Voice Status & Feedback Banner
        AnimatedVisibility(
            visible = isVoiceListening || voiceStatusMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceDark)
                    .border(1.dp, if (isVoiceListening) CyberPink else CyberCardBorder)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isVoiceListening) Icons.Default.Mic else Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = if (isVoiceListening) CyberPink else NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = voiceStatusMessage ?: "Listening... Ask about courses, fees, placements or hostels",
                    color = if (isVoiceListening) CyberPink else TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isVoiceListening) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberPink.copy(alpha = 0.2f))
                            .border(1.dp, CyberPink, RoundedCornerShape(6.dp))
                            .clickable { viewModel.stopVoiceRecognition() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STOP",
                            color = CyberPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Status",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.dismissVoiceStatus() }
                    )
                }
            }
        }

        // Bottom Input Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceDark)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone Button with Permission & Speech Recognition
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isVoiceListening) CyberPink else CyberCardDark)
                    .border(1.dp, if (isVoiceListening) CyberPink else CyberCardBorder, CircleShape)
                    .clickable { handleMicClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isVoiceListening) "Stop Listening" else "Voice Input",
                    tint = if (isVoiceListening) Color.White else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.onChatInputChange(it) },
                placeholder = { Text("Ask anything about college...", color = TextMuted, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.sendChatMessage() }),
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedContainerColor = CyberCardDark,
                    unfocusedContainerColor = CyberCardDark
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Stop TTS or Send Button
            if (isSpeakingAudio) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberEmerald)
                        .clickable { viewModel.stopAudioSpeech() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speech",
                        tint = Color(0xFF04101A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (chatInput.isNotBlank()) NeonCyan else CyberCardDark)
                        .clickable(enabled = chatInput.isNotBlank() && !isProcessing) {
                            viewModel.sendChatMessage()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (chatInput.isNotBlank()) Color(0xFF04101A) else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 16.dp
                        )
                    )
                    .background(if (isUser) UserBubble else AiBubble)
                    .border(
                        1.dp,
                        if (isUser) CyberCardBorder else NeonCyan.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isUser && message.sources != null) {
                            Text(
                                text = "Source: ${message.sources}",
                                color = NeonCyanLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = timeStr,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            if (!isUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clickable { onCopy() }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read out loud",
                                    tint = NeonCyan,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clickable { onSpeak() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
