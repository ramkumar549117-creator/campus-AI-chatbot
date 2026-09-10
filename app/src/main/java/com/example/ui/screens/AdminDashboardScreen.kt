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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FaqEntity
import com.example.ui.CampusViewModel
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: CampusViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminTab by viewModel.adminTab.collectAsState()

    val notices by viewModel.notices.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val faculty by viewModel.faculty.collectAsState()
    val faqs by viewModel.faqs.collectAsState()
    val campusResources by viewModel.campusResources.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    // Search and filtering state for Knowledge Base entries
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Dialog states for creating and editing knowledge base items
    var showFaqDialog by remember { mutableStateOf(false) }
    var editingFaq by remember { mutableStateOf<FaqEntity?>(null) }
    var showNoticeDialog by remember { mutableStateOf(false) }
    var showCourseDialog by remember { mutableStateOf(false) }
    var showResourceDialog by remember { mutableStateOf(false) }
    var showStudentEditDialog by remember { mutableStateOf(false) }

    // Filter FAQs based on search and category
    val filteredFaqs = remember(faqs, searchQuery, selectedCategoryFilter) {
        faqs.filter { faq ->
            val matchesCategory = selectedCategoryFilter == "All" ||
                    faq.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    faq.question.contains(searchQuery, ignoreCase = true) ||
                    faq.answer.contains(searchQuery, ignoreCase = true) ||
                    faq.keywords.contains(searchQuery, ignoreCase = true) ||
                    faq.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    if (!isAdminLoggedIn) {
        // Admin Login Form
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CyberDarkBg)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("admin_login_card")) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CampusAI Admin Console",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage College Knowledge Base & RAG Index",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            loginError = false
                        },
                        label = { Text("Admin Password (default: admin123)", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder
                        )
                    )

                    if (loginError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Invalid credentials. Try 'admin123'",
                            color = CyberPink,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    CyberNeonButton(
                        text = "LOGIN AS ADMINISTRATOR",
                        onClick = {
                            if (viewModel.loginAdmin(passwordInput)) {
                                loginError = false
                            } else {
                                loginError = true
                            }
                        },
                        icon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth().testTag("admin_login_submit_button")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { viewModel.loginAdmin("admin123") },
                        modifier = Modifier.testTag("admin_demo_login_button")
                    ) {
                        Text(text = "One-Tap Demo Login (admin123)", color = NeonCyanLight, fontSize = 12.sp)
                    }
                }
            }
        }
        return
    }

    // Foundational Admin Dashboard Layout
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .testTag("admin_dashboard_layout"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Foundational Header with System Status & Logout
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("admin_header_card")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROOM DB CONNECTED • RAG INDEX ACTIVE",
                                color = CyberEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Admin Dashboard",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "College Knowledge Base & Conversational Grounding",
                            color = NeonCyanLight,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.logoutAdmin() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberPink.copy(alpha = 0.5f))),
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Text(text = "Logout", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Foundational Knowledge Base Management Action Deck
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("kb_management_deck")) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KNOWLEDGE BASE ACTIONS",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Manage Grounding Data",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons for managing knowledge base entries (FAQ, Resources, Notices, Courses)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("kb_action_buttons_row")
                    ) {
                        item {
                            KnowledgeBaseActionButton(
                                title = "Add FAQ Item",
                                subtitle = "Ground AI Q&A",
                                icon = Icons.Default.QuestionAnswer,
                                accentColor = NeonCyan,
                                testTag = "btn_action_add_faq",
                                onClick = {
                                    viewModel.setAdminTab(0)
                                    showFaqDialog = true
                                }
                            )
                        }
                        item {
                            KnowledgeBaseActionButton(
                                title = "Add Facility",
                                subtitle = "Labs & Services",
                                icon = Icons.Default.School,
                                accentColor = CyberEmerald,
                                testTag = "btn_action_add_resource",
                                onClick = {
                                    viewModel.setAdminTab(1)
                                    showResourceDialog = true
                                }
                            )
                        }
                        item {
                            KnowledgeBaseActionButton(
                                title = "Publish Notice",
                                subtitle = "Campus Circular",
                                icon = Icons.Default.Campaign,
                                accentColor = CyberAmber,
                                testTag = "btn_action_add_notice",
                                onClick = {
                                    viewModel.setAdminTab(3)
                                    showNoticeDialog = true
                                }
                            )
                        }
                        item {
                            KnowledgeBaseActionButton(
                                title = "Add Course",
                                subtitle = "Degree Program",
                                icon = Icons.Default.MenuBook,
                                accentColor = CyberViolet,
                                testTag = "btn_action_add_course",
                                onClick = {
                                    viewModel.setAdminTab(4)
                                    showCourseDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Knowledge Base Metrics & Quick Jump Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatTile(
                    value = "${faqs.size}",
                    label = "FAQs",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_card_faqs",
                    onClick = { viewModel.setAdminTab(0) }
                )
                AdminStatTile(
                    value = "${campusResources.size}",
                    label = "Resources",
                    color = CyberEmerald,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_card_resources",
                    onClick = { viewModel.setAdminTab(1) }
                )
                AdminStatTile(
                    value = "${notices.size}",
                    label = "Notices",
                    color = CyberAmber,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_card_notices",
                    onClick = { viewModel.setAdminTab(3) }
                )
                AdminStatTile(
                    value = "${courses.size}",
                    label = "Courses",
                    color = CyberViolet,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_card_courses",
                    onClick = { viewModel.setAdminTab(4) }
                )
            }
        }

        // 4. Universal Search & Category Filter Bar
        item {
            NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("admin_search_bar_card")) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search FAQs, resources, notices, keywords...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_kb_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder
                        )
                    )

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_category_filter_row")
                    ) {
                        val categories = listOf("All", "Admission", "Academics", "Hostel", "Examinations", "Facilities", "Placement", "Fees")
                        items(categories) { cat ->
                            CyberChip(
                                text = cat,
                                isSelected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat }
                            )
                        }
                    }
                }
            }
        }

        // 5. Section Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_section_tabs_row")
            ) {
                val tabs = listOf(
                    "FAQs (${faqs.size})",
                    "Resources (${campusResources.size})",
                    "Student Profile",
                    "Notices (${notices.size})",
                    "Courses (${courses.size})",
                    "Faculty (${faculty.size})",
                    "Analytics"
                )
                items(tabs.indices.toList()) { idx ->
                    CyberChip(
                        text = tabs[idx],
                        isSelected = adminTab == idx,
                        onClick = { viewModel.setAdminTab(idx) }
                    )
                }
            }
        }

        // ==========================================
        // TAB 0: FAQs Knowledge Base Management
        // ==========================================
        if (adminTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Knowledge Base FAQs",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredFaqs.size} grounded Q&A entries available",
                            color = NeonCyanLight,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showFaqDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("add_faq_primary_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF04101A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add FAQ", color = Color(0xFF04101A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredFaqs.isEmpty()) {
                item {
                    NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("empty_faqs_card")) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No FAQ entries matching \"$searchQuery\"" else "No FAQs found in this category",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (searchQuery.isNotBlank() || selectedCategoryFilter != "All") {
                                    OutlinedButton(onClick = {
                                        searchQuery = ""
                                        selectedCategoryFilter = "All"
                                    }) {
                                        Text("Reset Filters", fontSize = 11.sp)
                                    }
                                }
                                Button(
                                    onClick = { showFaqDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                ) {
                                    Text("Add New FAQ", color = Color(0xFF04101A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredFaqs, key = { it.id }) { faq ->
                    NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("faq_card_${faq.id}")) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Row: Category tag and ID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonCyan.copy(alpha = 0.15f))
                                        .border(0.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = faq.category.uppercase(Locale.ROOT),
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "FAQ #${faq.id}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Question
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "Q:",
                                    color = NeonCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = faq.question,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Answer
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "A:",
                                    color = CyberEmerald,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = faq.answer,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Keywords tag
                            Text(
                                text = "RAG Keywords: ${faq.keywords}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons for managing this FAQ entry
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { editingFaq = faq },
                                    modifier = Modifier.testTag("btn_edit_faq_${faq.id}"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanLight),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.4f)))
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit FAQ", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = { viewModel.removeFaq(faq.id) },
                                    modifier = Modifier.testTag("btn_delete_faq_${faq.id}"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberPink.copy(alpha = 0.4f)))
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete FAQ", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: Campus Resources Management
        // ==========================================
        if (adminTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Campus Resources & Services", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Facilities, research labs, shuttles & services", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showResourceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                        modifier = Modifier.testTag("btn_add_resource_primary")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF04101A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Resource", color = Color(0xFF04101A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(campusResources, key = { it.id }) { r ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("resource_card_${r.id}")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = r.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = r.category, color = CyberEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = "📍 ${r.location} • ⏰ ${r.availability}", color = CyberEmerald, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = r.description, color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = "Access: ${r.accessDetails} | Contact: ${r.contactInfo}", color = TextMuted, fontSize = 10.sp)
                        }
                        IconButton(
                            onClick = { viewModel.removeCampusResource(r.id) },
                            modifier = Modifier.testTag("btn_delete_resource_${r.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 2: Student Information Record
        // ==========================================
        if (adminTab == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Student Information Records", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Room Local Database Profile", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showStudentEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                        modifier = Modifier.testTag("btn_edit_student_record")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Record", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val p = studentProfile
            item {
                NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyberViolet.copy(alpha = 0.2f))
                                    .border(1.dp, CyberViolet, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = CyberViolet)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = p?.name ?: "Priya Sharma", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Roll: ${p?.rollNo ?: "2023BCSE042"} • ${p?.department ?: "CSE"}, Sem ${p?.semester ?: "6"}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("CGPA", color = TextMuted, fontSize = 10.sp)
                                Text("${p?.cgpa ?: 8.84} / 10", color = CyberEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Attendance", color = TextMuted, fontSize = 10.sp)
                                Text("${p?.attendancePct ?: 85}%", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Fee Status", color = TextMuted, fontSize = 10.sp)
                                Text(p?.feeDues ?: "Cleared (₹0)", color = CyberAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Hostel Room", color = TextMuted, fontSize = 10.sp)
                                Text(p?.hostelRoom ?: "Block 4, 302", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 3: College Notices & Circulars
        // ==========================================
        if (adminTab == 3) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Campus Circulars (${notices.size})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showNoticeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                        modifier = Modifier.testTag("btn_add_notice_primary")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF04101A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Notice", color = Color(0xFF04101A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(notices, key = { it.id }) { n ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("notice_card_${n.id}")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = n.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${n.date} • ${n.category} • ${n.department}", color = CyberAmber, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = n.content, color = TextSecondary, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { viewModel.removeNotice(n.id) },
                            modifier = Modifier.testTag("btn_delete_notice_${n.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 4: Courses & Academic Programs
        // ==========================================
        if (adminTab == 4) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Academic Programs (${courses.size})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showCourseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                        modifier = Modifier.testTag("btn_add_course_primary")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(courses, key = { it.id }) { c ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("course_card_${c.id}")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${c.name} (${c.degree})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Code: ${c.code} • Fee: ${c.feesPerYear} • Intake: ${c.intake}", color = NeonCyanLight, fontSize = 11.sp)
                            Text(text = "Eligibility: ${c.eligibility}", color = TextSecondary, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { viewModel.removeCourse(c.id) },
                            modifier = Modifier.testTag("btn_delete_course_${c.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 5: Faculty Management
        // ==========================================
        if (adminTab == 5) {
            item {
                Text(text = "Faculty Directory (${faculty.size})", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            items(faculty, key = { it.id }) { f ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth().testTag("faculty_card_${f.id}")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = f.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${f.designation} • ${f.department}", color = CyberViolet, fontSize = 11.sp)
                            Text(text = "Cabin: ${f.cabin} • Office Hours: ${f.officeHours}", color = TextSecondary, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { viewModel.removeFaculty(f.id) },
                            modifier = Modifier.testTag("btn_delete_faculty_${f.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 6: Analytics & Insights
        // ==========================================
        if (adminTab == 6) {
            item {
                NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Top Student Inquiry Topics",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = NeonCyan)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val topics = listOf(
                            Pair("Mid-Semester Exam Dates & Hall Tickets", 0.88f),
                            Pair("Placement Packages & Eligibility Criteria", 0.74f),
                            Pair("Attendance Percentage & Exam Rules", 0.65f),
                            Pair("Semester Fee Payment Deadlines", 0.52f),
                            Pair("Central Library Timings & Books", 0.38f),
                            Pair("Hostel Mess Menus & Room Change", 0.29f)
                        )

                        topics.forEach { (topic, pct) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = topic, color = TextPrimary, fontSize = 12.sp)
                                    Text(text = "${(pct * 100).toInt()}%", color = NeonCyanLight, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = NeonCyan,
                                    trackColor = CyberCardDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS FOR KNOWLEDGE BASE ENTRIES MANAGEMENT
    // =========================================================================

    // 1. ADD FAQ DIALOG
    if (showFaqDialog) {
        var qText by remember { mutableStateOf("") }
        var aText by remember { mutableStateOf("") }
        var qCategory by remember { mutableStateOf("Admission") }
        var qKw by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("Add Knowledge Base FAQ", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qText,
                        onValueChange = { qText = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth().testTag("input_faq_question")
                    )
                    OutlinedTextField(
                        value = aText,
                        onValueChange = { aText = it },
                        label = { Text("Answer (Detailed RAG Response)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_faq_answer"),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = qCategory,
                        onValueChange = { qCategory = it },
                        label = { Text("Category (Admission, Hostel, Exam, Fees, Academics)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_faq_category")
                    )
                    OutlinedTextField(
                        value = qKw,
                        onValueChange = { qKw = it },
                        label = { Text("Search Keywords (space-separated tokens)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_faq_keywords")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (qText.isNotBlank() && aText.isNotBlank()) {
                            viewModel.createFaq(
                                question = qText.trim(),
                                answer = aText.trim(),
                                category = qCategory.trim(),
                                keywords = qKw.ifEmpty { qText }.trim()
                            )
                            showFaqDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_save_faq")
                ) {
                    Text("Index FAQ Entry")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFaqDialog = false },
                    modifier = Modifier.testTag("btn_cancel_faq")
                ) {
                    Text("Cancel")
                }
            },
            containerColor = CyberSurfaceDark
        )
    }

    // 2. EDIT FAQ DIALOG
    editingFaq?.let { targetFaq ->
        var editQuestion by remember { mutableStateOf(targetFaq.question) }
        var editAnswer by remember { mutableStateOf(targetFaq.answer) }
        var editCategory by remember { mutableStateOf(targetFaq.category) }
        var editKeywords by remember { mutableStateOf(targetFaq.keywords) }

        AlertDialog(
            onDismissRequest = { editingFaq = null },
            title = { Text("Edit Knowledge Base FAQ (#${targetFaq.id})", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editQuestion,
                        onValueChange = { editQuestion = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_faq_question")
                    )
                    OutlinedTextField(
                        value = editAnswer,
                        onValueChange = { editAnswer = it },
                        label = { Text("Answer") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_faq_answer"),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_faq_category")
                    )
                    OutlinedTextField(
                        value = editKeywords,
                        onValueChange = { editKeywords = it },
                        label = { Text("Keywords") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_faq_keywords")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editQuestion.isNotBlank() && editAnswer.isNotBlank()) {
                            viewModel.updateFaq(
                                id = targetFaq.id,
                                question = editQuestion.trim(),
                                answer = editAnswer.trim(),
                                category = editCategory.trim(),
                                keywords = editKeywords.trim()
                            )
                            editingFaq = null
                        }
                    },
                    modifier = Modifier.testTag("btn_update_faq")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingFaq = null },
                    modifier = Modifier.testTag("btn_cancel_edit_faq")
                ) {
                    Text("Cancel")
                }
            },
            containerColor = CyberSurfaceDark
        )
    }

    // 3. ADD NOTICE DIALOG
    if (showNoticeDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Academic") }
        var newDept by remember { mutableStateOf("Computer Science") }
        var newContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNoticeDialog = false },
            title = { Text("Add College Circular / Notice", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Category (Exam, Event, Placement, Academic)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Notice Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTitle.isNotBlank() && newContent.isNotBlank()) {
                        viewModel.createNotice(newTitle, newCategory, newDept, newContent)
                        showNoticeDialog = false
                    }
                }) {
                    Text("Publish Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoticeDialog = false }) { Text("Cancel") }
            },
            containerColor = CyberSurfaceDark
        )
    }

    // 4. ADD COURSE DIALOG
    if (showCourseDialog) {
        var cCode by remember { mutableStateOf("") }
        var cName by remember { mutableStateOf("") }
        var cDept by remember { mutableStateOf("Engineering") }
        var cDegree by remember { mutableStateOf("B.Tech") }
        var cFees by remember { mutableStateOf("₹1,80,000 / year") }

        AlertDialog(
            onDismissRequest = { showCourseDialog = false },
            title = { Text("Add Academic Course", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cCode, onValueChange = { cCode = it }, label = { Text("Course Code (e.g. CS105)") })
                    OutlinedTextField(value = cName, onValueChange = { cName = it }, label = { Text("Course Name") })
                    OutlinedTextField(value = cDegree, onValueChange = { cDegree = it }, label = { Text("Degree") })
                    OutlinedTextField(value = cFees, onValueChange = { cFees = it }, label = { Text("Annual Fees (e.g. ₹1,80,000 / year)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (cCode.isNotBlank() && cName.isNotBlank()) {
                        viewModel.createCourse(cCode, cName, cDept, cDegree, "4 Years", cFees, 60, "10+2 with 60%", "Comprehensive curriculum")
                        showCourseDialog = false
                    }
                }) {
                    Text("Save Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCourseDialog = false }) { Text("Cancel") }
            },
            containerColor = CyberSurfaceDark
        )
    }

    // 5. ADD CAMPUS RESOURCE DIALOG
    if (showResourceDialog) {
        var rTitle by remember { mutableStateOf("") }
        var rCategory by remember { mutableStateOf("Facility") }
        var rLocation by remember { mutableStateOf("") }
        var rAvailability by remember { mutableStateOf("Mon-Fri 8:00 AM - 6:00 PM") }
        var rAccess by remember { mutableStateOf("Valid Student ID card required") }
        var rContact by remember { mutableStateOf("") }
        var rDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showResourceDialog = false },
            title = { Text("Add Campus Resource / Facility", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = rTitle, onValueChange = { rTitle = it }, label = { Text("Resource Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rCategory, onValueChange = { rCategory = it }, label = { Text("Category (Compute, Lab, Transport, Health)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rLocation, onValueChange = { rLocation = it }, label = { Text("Campus Location") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rAvailability, onValueChange = { rAvailability = it }, label = { Text("Hours / Timings") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rDesc, onValueChange = { rDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (rTitle.isNotBlank()) {
                        viewModel.createCampusResource(
                            title = rTitle,
                            category = rCategory,
                            location = rLocation,
                            availability = rAvailability,
                            accessDetails = rAccess,
                            contactInfo = rContact.ifEmpty { "Admin Desk Ext. 100" },
                            description = rDesc
                        )
                        showResourceDialog = false
                    }
                }) {
                    Text("Save Resource")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResourceDialog = false }) { Text("Cancel") }
            },
            containerColor = CyberSurfaceDark
        )
    }

    // 6. UPDATE STUDENT PROFILE DIALOG
    if (showStudentEditDialog) {
        val curr = studentProfile
        var sName by remember { mutableStateOf(curr?.name ?: "Priya Sharma") }
        var sEmail by remember { mutableStateOf(curr?.email ?: "priya.s@campus.edu") }
        var sPhone by remember { mutableStateOf(curr?.phone ?: "+91 98765 43210") }
        var sDept by remember { mutableStateOf(curr?.department ?: "Computer Science & Engineering") }
        var sSem by remember { mutableStateOf(curr?.semester ?: "6") }
        var sCgpa by remember { mutableStateOf(curr?.cgpa?.toString() ?: "8.84") }
        var sAtt by remember { mutableStateOf(curr?.attendancePct?.toString() ?: "85") }
        var sFee by remember { mutableStateOf(curr?.feeDues ?: "Cleared (₹0 pending)") }
        var sHostel by remember { mutableStateOf(curr?.hostelRoom ?: "Hostel Block 4, Room 302") }
        var sMentor by remember { mutableStateOf(curr?.mentor ?: "Dr. Alan Turing") }

        AlertDialog(
            onDismissRequest = { showStudentEditDialog = false },
            title = { Text("Update Student Record", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Student Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sPhone, onValueChange = { sPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sEmail, onValueChange = { sEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sDept, onValueChange = { sDept = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sCgpa, onValueChange = { sCgpa = it }, label = { Text("CGPA") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sAtt, onValueChange = { sAtt = it }, label = { Text("Attendance %") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = sFee, onValueChange = { sFee = it }, label = { Text("Fee Status") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sHostel, onValueChange = { sHostel = it }, label = { Text("Hostel & Room") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sMentor, onValueChange = { sMentor = it }, label = { Text("Faculty Mentor") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cgpaVal = sCgpa.toDoubleOrNull() ?: (curr?.cgpa ?: 8.84)
                    val attVal = sAtt.toIntOrNull() ?: (curr?.attendancePct ?: 85)
                    viewModel.updateStudentProfile(
                        name = sName,
                        email = sEmail,
                        phone = sPhone,
                        department = sDept,
                        semester = sSem,
                        cgpa = cgpaVal,
                        attendancePct = attVal,
                        feeDues = sFee,
                        hostelRoom = sHostel,
                        mentor = sMentor
                    )
                    showStudentEditDialog = false
                }) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStudentEditDialog = false }) { Text("Cancel") }
            },
            containerColor = CyberSurfaceDark
        )
    }
}

/**
 * Knowledge Base Action Button component for primary entry management.
 */
@Composable
private fun KnowledgeBaseActionButton(
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
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
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
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = accentColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AdminStatTile(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    NeonGlassCard(modifier = clickableModifier.testTag(testTag)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, color = TextSecondary, fontSize = 9.sp, maxLines = 1)
        }
    }
}
