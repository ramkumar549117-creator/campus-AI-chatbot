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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var passwordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    // Dialog states for creating new items
    var showNoticeDialog by remember { mutableStateOf(false) }
    var showCourseDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    if (!isAdminLoggedIn) {
        // Admin Login Form
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CyberDarkBg)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
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
                        text = "Secure access for college staff & administrators",
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
                        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { viewModel.loginAdmin("admin123") }) {
                        Text(text = "One-Tap Demo Login (admin123)", color = NeonCyanLight, fontSize = 12.sp)
                    }
                }
            }
        }
        return
    }

    // Logged In Admin Dashboard
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Admin Control Center",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage College Knowledge Base & RAG Index",
                        color = NeonCyanLight,
                        fontSize = 12.sp
                    )
                }

                TextButton(onClick = { viewModel.logoutAdmin() }) {
                    Text(text = "Logout", color = CyberPink, fontSize = 13.sp)
                }
            }
        }

        // Live Statistics Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatTile("2,840", "Total AI Queries", NeonCyan, Modifier.weight(1f))
                AdminStatTile("1,250", "Active Students", CyberEmerald, Modifier.weight(1f))
                AdminStatTile("${courses.size + notices.size + faqs.size}", "RAG Knowledge Items", CyberViolet, Modifier.weight(1f))
                AdminStatTile("99.9%", "System Uptime", CyberAmber, Modifier.weight(1f))
            }
        }

        // Section Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf("Analytics", "Notices (${notices.size})", "Courses (${courses.size})", "Faculty (${faculty.size})", "FAQs (${faqs.size})")
                items(tabs.indices.toList()) { idx ->
                    CyberChip(
                        text = tabs[idx],
                        isSelected = adminTab == idx,
                        onClick = { viewModel.setAdminTab(idx) }
                    )
                }
            }
        }

        // TAB 0: Analytics of Student Queries
        if (adminTab == 0) {
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

        // TAB 1: Notices Management
        if (adminTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Published Circulars", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showNoticeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF04101A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Notice", color = Color(0xFF04101A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(notices) { n ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = n.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${n.date} • ${n.category} • ${n.department}", color = NeonCyanLight, fontSize = 11.sp)
                            Text(text = n.content, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                        }
                        IconButton(onClick = { viewModel.removeNotice(n.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // TAB 2: Courses Management
        if (adminTab == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Academic Programs", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showCourseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF04101A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", color = Color(0xFF04101A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(courses) { c ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
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
                        IconButton(onClick = { viewModel.removeCourse(c.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // TAB 3: Faculty Management
        if (adminTab == 3) {
            item {
                Text(text = "Faculty Directory", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            items(faculty) { f ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
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
                        IconButton(onClick = { viewModel.removeFaculty(f.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }

        // TAB 4: FAQs & Knowledge Base
        if (adminTab == 4) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Indexed FAQs (Instant RAG Match)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showFaqDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF04101A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add FAQ", color = Color(0xFF04101A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(faqs) { faq ->
                NeonGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Q: ${faq.question}", color = NeonCyanLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = "A: ${faq.answer}", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Category: ${faq.category} • Keywords: ${faq.keywords}", color = TextMuted, fontSize = 10.sp)
                        }
                        IconButton(onClick = { viewModel.removeFaq(faq.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }
    }

    // Add Notice Dialog
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

    // Add Course Dialog
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

    // Add FAQ Dialog
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
                    OutlinedTextField(value = qText, onValueChange = { qText = it }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("Answer") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    OutlinedTextField(value = qCategory, onValueChange = { qCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qKw, onValueChange = { qKw = it }, label = { Text("Keywords (space-separated)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (qText.isNotBlank() && aText.isNotBlank()) {
                        viewModel.createFaq(qText, aText, qCategory, qKw.ifEmpty { qText })
                        showFaqDialog = false
                    }
                }) {
                    Text("Index FAQ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFaqDialog = false }) { Text("Cancel") }
            },
            containerColor = CyberSurfaceDark
        )
    }
}

@Composable
private fun AdminStatTile(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, color = TextSecondary, fontSize = 9.sp, maxLines = 1)
        }
    }
}
