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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FaqEntity
import com.example.ui.AppScreen
import com.example.ui.CampusViewModel
import com.example.ui.components.CyberChip
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.CyberAmber
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
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val studentProfile by viewModel.studentProfile.collectAsState()
    val faqs by viewModel.faqs.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val profile = studentProfile

    // Interactive query input state for students to directly query the RAG knowledge base
    var queryInput by remember { mutableStateOf("") }
    var selectedFaqCategory by remember { mutableStateOf("All") }

    // Frequently asked suggested queries for 1-tap RAG retrieval
    val suggestedQueries = listOf(
        "When are Spring 2026 mid-term exams?",
        "What are the central library timings?",
        "How do I pay my semester fees online?",
        "What is the hostel room change process?",
        "What is the placement eligibility criteria?",
        "Where is the campus shuttle bus schedule?",
        "How do I access the AI & GPU Research Lab?"
    )

    // Filter FAQs by category
    val displayedFaqs = remember(faqs, selectedFaqCategory) {
        if (selectedFaqCategory == "All") {
            faqs.take(5)
        } else {
            faqs.filter { it.category.equals(selectedFaqCategory, ignoreCase = true) }
        }
    }

    fun submitStudentQuery(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isNotEmpty()) {
            viewModel.navigateTo(AppScreen.CHAT)
            viewModel.sendChatMessage(overridePrompt = cleanPrompt)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .testTag("student_dashboard_layout"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Compact Student Profile & Status Summary
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("student_profile_summary_card")) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.5.dp, NeonCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Student Avatar",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = profile?.name ?: "Alex Rivera",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${profile?.department ?: "CSE"} • ${profile?.rollNo ?: "CS2023089"}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // System RAG Ready Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberEmerald.copy(alpha = 0.15f))
                                .border(0.5.dp, CyberEmerald, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CyberEmerald)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RAG ONLINE",
                                    color = CyberEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Key Metrics Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudentMetricPill(
                            label = "Attendance",
                            value = "${profile?.attendancePct ?: 85}%",
                            accentColor = if ((profile?.attendancePct ?: 85) >= 75) CyberEmerald else CyberPink,
                            modifier = Modifier.weight(1f)
                        )
                        StudentMetricPill(
                            label = "CGPA",
                            value = "${profile?.cgpa ?: 8.84}",
                            accentColor = CyberViolet,
                            modifier = Modifier.weight(1f)
                        )
                        StudentMetricPill(
                            label = "Fee Dues",
                            value = profile?.feeDues ?: "Cleared",
                            accentColor = CyberAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Primary RAG AI Assistant Query Box (Hero Component)
        item {
            NeonGlassCard(
                modifier = Modifier.fillMaxWidth().testTag("rag_query_hero_card"),
                borderColor = NeonCyan.copy(alpha = 0.6f),
                backgroundColor = Color(0xFF071B2B)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Ask CampusAI Assistant",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Grounded by Gemini API & verified college knowledge base",
                                color = NeonCyanLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Field with Action Button
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        placeholder = {
                            Text(
                                text = "Ask about exams, fees, library, hostel, placements...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = NeonCyan
                            )
                        },
                        trailingIcon = {
                            if (queryInput.isNotBlank()) {
                                IconButton(onClick = { queryInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            submitStudentQuery(queryInput)
                        }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_rag_query_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedContainerColor = CyberCardDark.copy(alpha = 0.8f),
                            unfocusedContainerColor = CyberCardDark.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ask Assistant Button
                    Button(
                        onClick = {
                            submitStudentQuery(queryInput.ifBlank { "What are the latest college updates?" })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_ask_ai_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Query",
                            tint = Color(0xFF04101A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Query Knowledge Base",
                            color = Color(0xFF04101A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. 1-Tap Suggested Knowledge Base Queries
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FREQUENT RAG INQUIRIES",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "1-Tap AI Answers",
                        color = NeonCyanLight,
                        fontSize = 10.sp
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("suggested_queries_row")
                ) {
                    items(suggestedQueries) { q ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CyberCardDark)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
                                .clickable { submitStudentQuery(q) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("quick_query_chip")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = q,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Knowledge Base Topic Explorers
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EXPLORE KNOWLEDGE TOPICS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RagTopicCard(
                        title = "Exams & Rules",
                        subtitle = "Schedules & Venue",
                        icon = Icons.Default.Event,
                        accentColor = CyberAmber,
                        modifier = Modifier.weight(1f),
                        testTag = "topic_card_exams",
                        onClick = { submitStudentQuery("Show me the mid-term exam schedule and rules") }
                    )
                    RagTopicCard(
                        title = "Fees & Dues",
                        subtitle = "Payment Deadlines",
                        icon = Icons.Default.Payment,
                        accentColor = CyberEmerald,
                        modifier = Modifier.weight(1f),
                        testTag = "topic_card_fees",
                        onClick = { submitStudentQuery("What are the fee payment guidelines and deadlines?") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RagTopicCard(
                        title = "Campus Labs",
                        subtitle = "Library & Transit",
                        icon = Icons.Default.School,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f),
                        testTag = "topic_card_facilities",
                        onClick = { submitStudentQuery("What are the central library and laboratory timings?") }
                    )
                    RagTopicCard(
                        title = "Hostel Living",
                        subtitle = "Mess & Residence",
                        icon = Icons.Default.Home,
                        accentColor = CyberViolet,
                        modifier = Modifier.weight(1f),
                        testTag = "topic_card_hostel",
                        onClick = { submitStudentQuery("Tell me about hostel room facilities and mess timings") }
                    )
                }
            }
        }

        // 5. Verified Knowledge Base FAQ Entries from Room DB
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GROUNDED FAQ KNOWLEDGE BASE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${faqs.size} Indexed Entries",
                    color = NeonCyanLight,
                    fontSize = 10.sp
                )
            }
        }

        // Category Filter for FAQs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().testTag("faq_categories_row")
            ) {
                val categories = listOf("All", "Admission", "Academics", "Facilities", "Hostel", "Examinations")
                items(categories) { cat ->
                    CyberChip(
                        text = cat,
                        isSelected = selectedFaqCategory == cat,
                        onClick = { selectedFaqCategory = cat }
                    )
                }
            }
        }

        items(displayedFaqs, key = { it.id }) { faq ->
            NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("student_faq_card_${faq.id}")) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(0.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = faq.category.uppercase(Locale.ROOT),
                                color = NeonCyanLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Grounded Item #${faq.id}",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = faq.question,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = faq.answer,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { submitStudentQuery("Explain more about: ${faq.question}") }
                            .testTag("btn_ask_faq_${faq.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ask AI Assistant for Details",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 6. Latest Campus Circulars with 1-Click AI Query
        if (notices.isNotEmpty()) {
            item {
                Text(
                    text = "LATEST CIRCULARS & NOTICES",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(notices.take(2), key = { it.id }) { notice ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("notice_item_${notice.id}")) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notice.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notice.date,
                                color = CyberAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = notice.content,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { submitStudentQuery("Tell me more about the notice: ${notice.title}") }
                                .testTag("notice_ask_ai_${notice.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = NeonCyanLight,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ask AI about this circular",
                                color = NeonCyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact pill component for student metrics.
 */
@Composable
private fun StudentMetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberCardDark)
            .border(0.5.dp, CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = value, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Compact card for launching knowledge topic RAG queries.
 */
@Composable
private fun RagTopicCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberCardDark)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
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
}
