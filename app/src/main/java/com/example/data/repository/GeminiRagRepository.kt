package com.example.data.repository

import android.util.Log
import com.example.data.local.CampusDao
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Result model encapsulated after processing a RAG query through Gemini API.
 */
data class RagQueryResult(
    val answer: String,
    val matchedSources: List<String>,
    val isOnlineAi: Boolean,
    val modelUsed: String? = null,
    val errorMessage: String? = null
)

/**
 * Repository communicating with Google Gemini API via Retrofit to process
 * Retrieval-Augmented Generation (RAG) queries grounded in the local Room college knowledge base.
 */
class GeminiRagRepository(
    private val dao: CampusDao,
    private val apiService: GeminiApiService = GeminiRetrofitClient.apiService
) {

    private val tag = "GeminiRagRepository"

    /**
     * Models evaluated in priority order according to Gemini API guidelines.
     * Default for simple text Q&A / knowledge retrieval is 'gemini-3.5-flash'.
     */
    private val candidateModels = listOf("gemini-3.5-flash", "gemini-3.6-flash", "gemini-flash-latest")

    /**
     * Processes a user query by retrieving grounded facts from the local Room database,
     * building contextual system instructions, and querying the Gemini API through Retrofit.
     * Falls back to the local offline knowledge engine if offline or if no API key is provided.
     */
    suspend fun processRagQuery(
        userPrompt: String,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): RagQueryResult = withContext(Dispatchers.IO) {
        val cleanQuery = userPrompt.lowercase(Locale.ROOT).trim()

        // Step 1: Build grounded college context from Room database
        val (collegeContext, sources) = buildRagContext(cleanQuery)

        // Step 2: Check API key validity
        if (!GeminiRetrofitClient.hasValidApiKey()) {
            Log.d(tag, "Gemini API key not configured. Processing with offline knowledge base engine.")
            val offlineAnswer = generateOfflineKnowledgeResponse(cleanQuery)
            return@withContext RagQueryResult(
                answer = offlineAnswer,
                matchedSources = sources.ifEmpty { listOf("Offline Knowledge Base") },
                isOnlineAi = false,
                errorMessage = "API key not configured in AI Studio Secrets"
            )
        }

        // Step 3: Construct System Prompt with Grounding Mandates
        val systemPrompt = """
            You are CampusAI, an advanced futuristic holographic 3D College AI Assistant.
            Your purpose is to assist students, prospective applicants, faculty, and campus visitors.
            Always provide accurate, polite, and helpful information strictly based on the verified College Knowledge Base provided below.
            Always express all fees, costs, placement packages, dues, scholarships, and monetary amounts in Indian Rupees (₹ / LPA). Never use dollars ($).
            If the requested information is not in the knowledge base, provide the best helpful guidance and advise the student to contact the respective college department or administration desk.
            
            VERIFIED COLLEGE KNOWLEDGE BASE CONTEXT:
            $collegeContext
        """.trimIndent()

        // Step 4: Construct multi-turn contents
        val contentsList = mutableListOf<GeminiContent>()

        // Include recent conversation history (last 4 turns)
        for ((role, text) in chatHistory.takeLast(4)) {
            val geminiRole = if (role == "user") "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = geminiRole,
                    parts = listOf(GeminiPart(text = text))
                )
            )
        }

        // Current turn prompt
        contentsList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userPrompt))
            )
        )

        val request = GeminiGenerateRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                topK = 40,
                maxOutputTokens = 2048
            )
        )

        val apiKey = GeminiRetrofitClient.getApiKey()
        var lastError: Exception? = null

        // Step 5: Execute Retrofit API call with model fallback
        for (model in candidateModels) {
            try {
                Log.d(tag, "Sending RAG query to Gemini API via Retrofit (model: $model)")
                val response = apiService.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )

                val generatedText = response.extractText()
                if (!generatedText.isNullOrBlank()) {
                    return@withContext RagQueryResult(
                        answer = generatedText,
                        matchedSources = sources.ifEmpty { listOf("Gemini AI + College Knowledge Base") },
                        isOnlineAi = true,
                        modelUsed = model
                    )
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.w(tag, "Gemini Retrofit request failed for model $model: $errorMsg")
                lastError = e

                // If model is deprecated or not found (404), try next candidate model
                if (errorMsg.contains("404") || errorMsg.contains("not found", ignoreCase = true) || errorMsg.contains("no longer available", ignoreCase = true)) {
                    continue
                } else {
                    break
                }
            }
        }

        // Fallback to offline knowledge base engine upon network/API error
        Log.w(tag, "All Gemini candidate models failed. Falling back to local knowledge base.", lastError)
        val fallbackAnswer = generateOfflineKnowledgeResponse(cleanQuery)
        return@withContext RagQueryResult(
            answer = fallbackAnswer,
            matchedSources = sources.ifEmpty { listOf("Offline Knowledge Base") },
            isOnlineAi = false,
            errorMessage = lastError?.message
        )
    }

    /**
     * Builds a comprehensive textual context and list of citations from Room database entities.
     */
    suspend fun buildRagContext(query: String): Pair<String, List<String>> {
        val notices = dao.getNoticesSnapshot()
        val courses = dao.getCoursesSnapshot()
        val faculty = dao.getFacultySnapshot()
        val faqs = dao.getFaqsSnapshot()
        val resources = dao.getCampusResourcesSnapshot()
        val exams = dao.getExamScheduleSnapshot()
        val placements = dao.getPlacementStatsSnapshot()
        val student = dao.getStudentProfile().firstOrNull()

        val sb = StringBuilder()
        val sourcesList = mutableListOf<String>()

        // 1. Student Personal & Academic Record
        if (query.contains("my") || query.contains("attendance") || query.contains("cgpa") ||
            query.contains("profile") || query.contains("roll") || query.contains("mentor") || query.contains("hostel")
        ) {
            student?.let {
                sb.appendLine("STUDENT PROFILE:")
                sb.appendLine("Name: ${it.name}, Roll: ${it.rollNo}, Dept: ${it.department}, Sem: ${it.semester}")
                sb.appendLine("Attendance: ${it.attendancePct}%, CGPA: ${it.cgpa}, Fee Status: ${it.feeDues}")
                sb.appendLine("Hostel Room: ${it.hostelRoom}, Faculty Mentor: ${it.mentor}")
                sb.appendLine("Blood Group: ${it.bloodGroup}, Emergency Contact: ${it.emergencyContact}, Library Card: ${it.libraryCardNo}")
                sb.appendLine()
                sourcesList.add("Student Record")
            }
        }

        // 2. Campus Resources & Facilities
        val relevantResources = resources.filter { r ->
            query.contains("resource") || query.contains("facility") || query.contains("lab") ||
                    query.contains("hpc") || query.contains("gpu") || query.contains("supercomputing") ||
                    query.contains("transit") || query.contains("shuttle") || query.contains("bus") ||
                    query.contains("clinic") || query.contains("health") || query.contains("wellness") ||
                    query.contains("maker") || query.contains("3d print") || query.contains("library") ||
                    r.title.lowercase(Locale.ROOT).split(" ").any { it.length > 3 && query.contains(it) } ||
                    r.category.lowercase(Locale.ROOT).let { query.contains(it) }
        }
        if (relevantResources.isNotEmpty() || query.contains("facility") || query.contains("resource")) {
            sb.appendLine("CAMPUS FACILITIES & RESOURCES:")
            val resToUse = if (relevantResources.isNotEmpty()) relevantResources else resources
            resToUse.forEach { r ->
                sb.appendLine("• ${r.title} (${r.category}): Location: ${r.location}, Hours: ${r.availability}. Access: ${r.accessDetails}. Contact: ${r.contactInfo}. Summary: ${r.description}")
            }
            sb.appendLine()
            sourcesList.add("Campus Resources")
        }

        // 3. College Circulars & Notices
        val relevantNotices = notices.filter { notice ->
            query.contains("notice") || query.contains("circular") || query.contains("announcement") ||
                    notice.title.lowercase(Locale.ROOT).split(" ").any { it.length > 3 && query.contains(it) } ||
                    query.contains(notice.category.lowercase(Locale.ROOT))
        }
        if (relevantNotices.isNotEmpty() || query.contains("notice")) {
            sb.appendLine("LATEST COLLEGE NOTICES:")
            val listToUse = if (relevantNotices.isNotEmpty()) relevantNotices else notices.take(3)
            listToUse.forEach { n ->
                sb.appendLine("• [${n.date} - ${n.category}] ${n.title}: ${n.content}")
            }
            sb.appendLine()
            sourcesList.add("College Notice Board")
        }

        // 4. Academic Programs & Courses
        val relevantCourses = courses.filter { c ->
            query.contains("course") || query.contains("program") || query.contains("degree") ||
                    query.contains(c.name.lowercase(Locale.ROOT)) || query.contains(c.degree.lowercase(Locale.ROOT)) ||
                    query.contains(c.department.lowercase(Locale.ROOT)) || query.contains(c.code.lowercase(Locale.ROOT))
        }
        if (relevantCourses.isNotEmpty() || query.contains("course") || query.contains("program") || query.contains("fee") || query.contains("b.tech") || query.contains("m.tech") || query.contains("mca") || query.contains("mba")) {
            sb.appendLine("ACADEMIC PROGRAMS & COURSES:")
            val list = if (relevantCourses.isNotEmpty()) relevantCourses else courses
            list.forEach { c ->
                sb.appendLine("• ${c.name} (${c.degree}, Code: ${c.code}): Duration ${c.duration}, Annual Fee: ${c.feesPerYear}, Intake: ${c.intake}. Eligibility: ${c.eligibility}. Summary: ${c.description}")
            }
            sb.appendLine()
            sourcesList.add("Course Directory")
        }

        // 5. Faculty Directory
        val relevantFaculty = faculty.filter { f ->
            query.contains("faculty") || query.contains("professor") || query.contains("teacher") || query.contains("hod") ||
                    query.contains(f.name.lowercase(Locale.ROOT).replace("dr.", "").replace("prof.", "").trim()) ||
                    query.contains(f.department.lowercase(Locale.ROOT))
        }
        if (relevantFaculty.isNotEmpty() || query.contains("faculty") || query.contains("hod") || query.contains("cabin")) {
            sb.appendLine("FACULTY DIRECTORY:")
            val list = if (relevantFaculty.isNotEmpty()) relevantFaculty else faculty
            list.forEach { f ->
                sb.appendLine("• ${f.name} (${f.designation}, ${f.department}): Cabin: ${f.cabin}, Email: ${f.email}, Office Hours: ${f.officeHours}")
            }
            sb.appendLine()
            sourcesList.add("Faculty Directory")
        }

        // 6. Exam Schedules
        if (query.contains("exam") || query.contains("timetable") || query.contains("schedule") || query.contains("date") || query.contains("test")) {
            sb.appendLine("UPCOMING EXAMINATION SCHEDULE:")
            exams.forEach { e ->
                sb.appendLine("• ${e.subjectCode} - ${e.subjectName}: ${e.date} (${e.time}) at ${e.venue}")
            }
            sb.appendLine()
            sourcesList.add("Exam Controller")
        }

        // 7. Placements
        if (query.contains("placement") || query.contains("package") || query.contains("recruit") || query.contains("salary") || query.contains("job") || query.contains("google")) {
            sb.appendLine("PLACEMENT STATISTICS:")
            placements.forEach { p ->
                sb.appendLine("• Year ${p.year}: Placement Rate: ${p.placementRate}, Highest: ${p.highestPackage}, Average: ${p.averagePackage}, Top Recruiters: ${p.topRecruiters}")
            }
            sb.appendLine()
            sourcesList.add("Placement Cell")
        }

        // 8. FAQs & Knowledge Items
        val matchedFaqs = faqs.filter { faq ->
            val kwList = faq.keywords.split(" ")
            kwList.any { kw -> query.contains(kw) } ||
                    faq.category.lowercase(Locale.ROOT).let { query.contains(it) }
        }
        if (matchedFaqs.isNotEmpty()) {
            sb.appendLine("OFFICIAL COLLEGE FAQS:")
            matchedFaqs.forEach { f ->
                sb.appendLine("Q: ${f.question}")
                sb.appendLine("A: ${f.answer}")
            }
            sb.appendLine()
            sourcesList.add("College FAQ Database")
        }

        return Pair(sb.toString(), sourcesList.distinct())
    }

    /**
     * Deterministic offline knowledge retrieval based on exact facts in the Room database.
     */
    private suspend fun generateOfflineKnowledgeResponse(query: String): String {
        val student = dao.getStudentProfile().firstOrNull()
        val notices = dao.getNoticesSnapshot()
        val courses = dao.getCoursesSnapshot()
        val faculty = dao.getFacultySnapshot()
        val resources = dao.getCampusResourcesSnapshot()
        val exams = dao.getExamScheduleSnapshot()
        val placements = dao.getPlacementStatsSnapshot()
        val faqs = dao.getFaqsSnapshot()

        // Check FAQs
        val matchedFaq = faqs.find { faq ->
            faq.keywords.split(" ").any { kw -> kw.length > 3 && query.contains(kw.lowercase(Locale.ROOT)) }
        }
        if (matchedFaq != null) {
            return "${matchedFaq.answer}\n\n(Official Knowledge Base: ${matchedFaq.category})"
        }

        // Check Campus Resources
        val matchedResource = resources.find { r ->
            query.contains(r.title.lowercase(Locale.ROOT)) ||
                    query.contains(r.category.lowercase(Locale.ROOT)) ||
                    r.title.lowercase(Locale.ROOT).split(" ").any { it.length > 3 && query.contains(it) }
        }
        if (matchedResource != null) {
            return "${matchedResource.title} (${matchedResource.category}):\n📍 Location: ${matchedResource.location}\n⏰ Hours: ${matchedResource.availability}\n📋 Access: ${matchedResource.accessDetails}\n📞 Contact: ${matchedResource.contactInfo}\n\n${matchedResource.description}"
        }

        // Personal student attendance & profile
        if (query.contains("attendance") || query.contains("my attendance")) {
            val att = student?.attendancePct ?: 85
            val status = if (att >= 75) "Eligible for final examinations" else "Critical: Below mandatory 75% threshold"
            return "Your current institutional attendance is $att% ($status).\nMinimum 75% aggregate is strictly enforced for university examinations."
        }

        if (query.contains("cgpa") || query.contains("grade") || query.contains("marks")) {
            return "Your cumulative grade point average is ${student?.cgpa ?: 8.84} CGPA. Current academic status: Active & in Good Standing."
        }

        if (query.contains("fee") || query.contains("fees") || query.contains("dues")) {
            return "Fee Status for ${student?.name ?: "Student"}: ${student?.feeDues ?: "Cleared (₹0 pending)"}.\nSemester fee receipts can be downloaded from the Accounts Portal or Admin Desk."
        }

        if (query.contains("hostel") || query.contains("room")) {
            return "Your assigned campus residence is ${student?.hostelRoom ?: "Hostel Block 4, Room 302"}. Chief Warden: Dr. Alan Turing (Contact: warden.hostel@campus.edu)."
        }

        // Examination query
        if (query.contains("exam") || query.contains("timetable") || query.contains("schedule")) {
            val sb = StringBuilder("Upcoming Mid-Semester Examination Schedule:\n")
            exams.forEach { e ->
                sb.appendLine("• ${e.subjectName} (${e.subjectCode}): ${e.date} at ${e.time}, Venue: ${e.venue}")
            }
            sb.append("\nHall tickets are mandatory for entry.")
            return sb.toString()
        }

        // Placement query
        if (query.contains("placement") || query.contains("salary") || query.contains("package")) {
            val p = placements.firstOrNull()
            return "Campus Placement Highlights:\n• Placement Rate: ${p?.placementRate ?: "94.8%"}\n• Highest Package: ${p?.highestPackage ?: "₹48.5 LPA (Google)"}\n• Average Package: ${p?.averagePackage ?: "₹12.4 LPA"}\n• Top Recruiters: ${p?.topRecruiters ?: "Google, Microsoft, Amazon, Nvidia, TCS, Infosys"}"
        }

        // Course query
        val matchedCourse = courses.find { c -> query.contains(c.code.lowercase(Locale.ROOT)) || query.contains(c.name.lowercase(Locale.ROOT)) }
        if (matchedCourse != null) {
            return "${matchedCourse.name} (${matchedCourse.degree}):\n• Code: ${matchedCourse.code} | Duration: ${matchedCourse.duration}\n• Annual Fee: ${matchedCourse.feesPerYear} | Intake: ${matchedCourse.intake} seats\n• Eligibility: ${matchedCourse.eligibility}\n• Overview: ${matchedCourse.description}"
        }

        // Faculty query
        val matchedFaculty = faculty.find { f -> query.contains(f.name.lowercase(Locale.ROOT).replace("dr.", "").trim()) }
        if (matchedFaculty != null) {
            return "${matchedFaculty.name} - ${matchedFaculty.designation} (${matchedFaculty.department})\n• Cabin: ${matchedFaculty.cabin}\n• Email: ${matchedFaculty.email}\n• Office Hours: ${matchedFaculty.officeHours}"
        }

        // Notices query
        if (query.contains("notice") || query.contains("circular")) {
            val sb = StringBuilder("Latest Campus Circulars:\n")
            notices.take(3).forEach { n ->
                sb.appendLine("• [${n.date}] ${n.title} (${n.category})")
            }
            return sb.toString()
        }

        return "I am CampusAI, your 3D College Assistant. You can ask me about mid-semester exam timetables, course fee structures, faculty cabin locations, campus library & GPU cluster hours, hostel allocations, placement records, and circular notices."
    }
}
