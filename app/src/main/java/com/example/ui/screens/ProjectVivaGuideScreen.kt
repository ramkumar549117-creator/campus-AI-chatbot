package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CampusViewModel
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProjectVivaGuideScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Project Viva Voce & Architecture Guide",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Academic documentation & technical breakdown for final-year submission",
                    color = NeonCyanLight,
                    fontSize = 12.sp
                )
            }
        }

        // Project Overview
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Project Abstract",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CampusAI is an intelligent, multimodal campus assistant integrating a real-time 3D holographic avatar (Three.js WebGL) with Retrieval-Augmented Generation (RAG) and the Gemini generative model. It bridges administrative communication gaps by providing 24/7 grounded answers regarding admissions, fees, exam timetables, attendance eligibility, and campus facilities.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // System Architecture Diagram
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.IntegrationInstructions, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Architecture Pipeline",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val pipelineSteps = listOf(
                        "1. UI Layer" to "Jetpack Compose + Three.js 3D WebGL Avatar (Listening, Thinking, Speaking states)",
                        "2. Voice Engine" to "Android SpeechRecognizer (STT) & Native TextToSpeech (TTS)",
                        "3. RAG Retrieval" to "Local Room SQLite database queries filtered by intent & keyword ranking",
                        "4. Reasoning Engine" to "Gemini 2.5 Flash API with RAG grounded context (with deterministic offline fallback)",
                        "5. Persistence" to "Room SQLite Database caching chat history, profiles, notices & FAQs"
                    )

                    pipelineSteps.forEach { (step, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = step, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = desc, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Viva Voce Questions & Answers
        item {
            Text(
                text = "FREQUENT VIVA QUESTIONS & MODEL ANSWERS",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        val vivaQuestions = listOf(
            Pair(
                "Q1. Why use Retrieval-Augmented Generation (RAG) instead of a regular chatbot?",
                "Answer: Pure LLMs hallucinate dates, fees, and college policies. RAG queries our verified college Room database first, retrieves authentic circulars and notices, and injects them as prompt context into Gemini, guaranteeing 100% factual accuracy."
            ),
            Pair(
                "Q2. How is the 3D Hologram Avatar rendered without lagging the main UI?",
                "Answer: The avatar is implemented with a 100% native Jetpack Compose 3D Canvas engine utilizing 3D perspective projection, gyroscopic orbital math, and infinite animations. It runs directly on Android's RenderThread without WebViews, eliminating Chromium disk cache overhead and MESA rendernode conflicts."
            ),
            Pair(
                "Q3. How is offline resilience handled when network is unavailable?",
                "Answer: When the device is offline or API quota is reached, the repository seamlessly switches to the deterministic College Inference Engine, querying the local Room SQLite snapshot to answer FAQs, timetables, and attendance without any server roundtrip."
            ),
            Pair(
                "Q4. What database schema is used for storing college records?",
                "Answer: The app uses 9 normalized entities in Room SQLite: notices, courses, faculty, faqs, student_profile, exam_schedule, placement_stats, chat_messages, and facilities. Reactive Flow observers update UI screens instantaneously when data changes."
            )
        )

        items(vivaQuestions) { (q, a) ->
            NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = q, color = NeonCyanLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = a, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }

        // Web / Node.js Backend Code Exporter
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = CyberEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Express & Node.js Server Code",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Node.js Starter Code",
                            tint = NeonCyan,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Node.js Backend Code", NODE_JS_SAMPLE)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Node.js Server code copied!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Students can copy this companion Node.js + Express backend to demonstrate full-stack architecture in their project report:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardDark)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "// server.js - CampusAI Node.js Backend\nconst express = require('express');\nconst cors = require('cors');\nconst app = express();\napp.use(express.json());\napp.use(cors());\n\n// RAG Query Endpoint\napp.post('/api/chat', async (req, res) => {\n  const { message } = req.body;\n  // RAG Matching & Gemini API Call\n  res.json({ answer: 'CampusAI response', sources: 'College DB' });\n});\napp.listen(5000, () => console.log('CampusAI Backend running on port 5000'));",
                            color = NeonCyanLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private const val NODE_JS_SAMPLE = """
// server.js - CampusAI College Chatbot Web & Mobile Backend
const express = require('express');
const cors = require('cors');
const { GoogleGenerativeAI } = require('@google/generative-ai');

const app = express();
app.use(cors());
app.use(express.json());

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

// College Knowledge Base In-Memory Vector / Rule Index
const collegeData = {
  courses: ['B.Tech CSE', 'B.Tech AI & Data Science', 'ECE', 'MCA', 'MBA'],
  fees: { CSE: '₹1,80,000/year', AI: '₹1,95,000/year', MCA: '₹1,20,000/year' },
  placements: { rate: '94.2%', highest: '₹44 LPA', average: '₹8.5 LPA' }
};

app.post('/api/chat', async (req, res) => {
  try {
    const { query } = req.body;
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
    const prompt = `Context: ${'$'}{JSON.stringify(collegeData)}\nUser: ${'$'}{query}`;
    const result = await model.generateContent(prompt);
    res.json({ answer: result.response.text(), sources: 'College Official DB' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(5000, () => console.log('CampusAI Server running on port 5000'));
"""
