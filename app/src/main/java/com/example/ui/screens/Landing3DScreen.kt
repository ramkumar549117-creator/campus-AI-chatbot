package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.CampusViewModel
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.HologramAvatarView
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.CyberAmberLight
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmeraldLight
import com.example.ui.theme.CyberPinkLight
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun Landing3DScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val avatarState by viewModel.avatarState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Header Badge
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAMPUS·AI — NEXT-GEN COLLEGE ASSISTANT",
                            color = NeonCyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Intelligent Campus AI",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "With Interactive 3D Holographic Avatar",
                    color = NeonCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3D Avatar Hero Canvas
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HologramAvatarView(
                    avatarState = avatarState,
                    height = 250.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                CyberNeonButton(
                    text = "START CHATTING WITH AI",
                    onClick = { viewModel.navigateTo(AppScreen.CHAT) },
                    icon = Icons.Default.Chat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Key College Metrics
        item {
            Text(
                text = "CAMPUS AT A GLANCE",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "94.2%",
                    subtitle = "Placement Rate",
                    color = CyberEmeraldLight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "₹44 LPA",
                    subtitle = "Highest Package",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "85,000+",
                    subtitle = "Library Books",
                    color = CyberViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Feature Highlights
        item {
            Text(
                text = "INNOVATION & CAPABILITIES",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureRow(
                    icon = Icons.Default.Psychology,
                    title = "RAG Verified College Knowledge Base",
                    description = "Answers strictly from college databases, notices, course guides, and exam cells — zero hallucinations.",
                    iconTint = NeonCyan
                )
                FeatureRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Interactive 3D WebGL Avatar",
                    description = "WebGL Three.js avatar with real-time head tracking, thinking cycles, and speaking waveforms.",
                    iconTint = CyberPinkLight
                )
                FeatureRow(
                    icon = Icons.Default.Mic,
                    title = "Speech-to-Text & Text-to-Speech",
                    description = "Natural hands-free voice interaction with native speech synthesis and live audio equalizers.",
                    iconTint = CyberEmeraldLight
                )
                FeatureRow(
                    icon = Icons.Default.School,
                    title = "Integrated Student Portal",
                    description = "Real-time attendance tracking with 75% exam warning gauge, exam schedules, and fee status.",
                    iconTint = CyberAmberLight
                )
                FeatureRow(
                    icon = Icons.Default.Security,
                    title = "Admin Management Console",
                    description = "Add notices, update courses, manage faculty directories, and monitor frequently asked student topics.",
                    iconTint = NeonCyanLight
                )
            }
        }

        // Quick Launch Actions
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Quick Inquiries",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to launch instant query in CampusAI Chat",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val sampleQuestions = listOf(
                        "What courses and fee structures are available?",
                        "What is the college attendance policy for exams?",
                        "Show latest notices and events",
                        "What are the hostel and mess facilities?"
                    )

                    sampleQuestions.forEach { question ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCardDark)
                                .clickable {
                                    viewModel.navigateTo(AppScreen.CHAT)
                                    viewModel.sendChatMessage(question)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = question,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color
) {
    NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
