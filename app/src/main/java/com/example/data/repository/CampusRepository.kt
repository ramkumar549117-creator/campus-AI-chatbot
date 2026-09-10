package com.example.data.repository

import android.util.Log
import com.example.data.local.CampusDao
import com.example.data.local.CampusResourceEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CollegeNoticeEntity
import com.example.data.local.CourseEntity
import com.example.data.local.ExamScheduleEntity
import com.example.data.local.FacultyEntity
import com.example.data.local.FaqEntity
import com.example.data.local.InitialDataSeeder
import com.example.data.local.PlacementStatEntity
import com.example.data.local.StudentProfileEntity
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Locale

class CampusRepository(private val dao: CampusDao) {

    private val tag = "CampusRepository"
    val geminiRagRepository = GeminiRagRepository(dao)

    // Reactive streams
    val allNotices: Flow<List<CollegeNoticeEntity>> = dao.getAllNotices()
    val allCourses: Flow<List<CourseEntity>> = dao.getAllCourses()
    val allFaculty: Flow<List<FacultyEntity>> = dao.getAllFaculty()
    val allFaqs: Flow<List<FaqEntity>> = dao.getAllFaqs()
    val allCampusResources: Flow<List<CampusResourceEntity>> = dao.getAllCampusResources()
    val allFacilities = dao.getAllFacilities()
    val examSchedule: Flow<List<ExamScheduleEntity>> = dao.getExamSchedule()
    val placementStats: Flow<List<PlacementStatEntity>> = dao.getPlacementStats()
    val studentProfile: Flow<StudentProfileEntity?> = dao.getStudentProfile()
    val allStudents: Flow<List<StudentProfileEntity>> = dao.getAllStudents()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun initializeData() {
        InitialDataSeeder.seedDatabaseIfEmpty(dao)
    }

    // Admin & Knowledge Base operations
    suspend fun addNotice(notice: CollegeNoticeEntity) = dao.insertNotice(notice)
    suspend fun deleteNotice(id: Int) = dao.deleteNotice(id)

    suspend fun addCourse(course: CourseEntity) = dao.insertCourse(course)
    suspend fun deleteCourse(id: Int) = dao.deleteCourse(id)

    suspend fun addFaculty(faculty: FacultyEntity) = dao.insertFaculty(faculty)
    suspend fun deleteFaculty(id: Int) = dao.deleteFaculty(id)

    suspend fun addFaq(faq: FaqEntity) = dao.insertFaq(faq)
    suspend fun updateFaq(faq: FaqEntity) = dao.updateFaq(faq)
    suspend fun deleteFaq(id: Int) = dao.deleteFaq(id)

    suspend fun addCampusResource(resource: CampusResourceEntity) = dao.insertCampusResource(resource)
    suspend fun updateCampusResource(resource: CampusResourceEntity) = dao.updateCampusResource(resource)
    suspend fun deleteCampusResource(id: Int) = dao.deleteCampusResource(id)

    suspend fun updateStudentProfile(profile: StudentProfileEntity) = dao.updateStudentProfile(profile)
    suspend fun insertStudentProfile(profile: StudentProfileEntity) = dao.insertStudentProfile(profile)

    suspend fun clearChat() = dao.clearChatHistory()

    /**
     * Intelligent RAG Chat Pipeline:
     * 1. Extracts keywords and classifies intent from user prompt.
     * 2. Retrieves relevant college documents from local Room database.
     * 3. Constructs context-grounded prompt.
     * 4. Attempts Gemini API call.
     * 5. Falls back to deterministic rule/FAQ engine if offline or no key.
     * 6. Persists turn to Room database.
     */
    suspend fun askAssistant(prompt: String, isVoice: Boolean = false): String = withContext(Dispatchers.IO) {
        // Save user message to database
        val userMsg = ChatMessageEntity(
            sender = "user",
            text = prompt,
            isVoice = isVoice
        )
        dao.insertChatMessage(userMsg)

        // Retrieve recent chat history for conversation continuity
        val recentMessages = dao.getAllChatMessages().firstOrNull() ?: emptyList()
        val history = recentMessages.takeLast(6).map { it.sender to it.text }

        // Process through Retrofit-backed Gemini RAG repository
        val ragResult = geminiRagRepository.processRagQuery(
            userPrompt = prompt,
            chatHistory = history
        )

        val sourcesText = if (ragResult.matchedSources.isNotEmpty()) {
            ragResult.matchedSources.joinToString(" • ")
        } else if (ragResult.isOnlineAi) {
            "Gemini AI (${ragResult.modelUsed ?: "gemini-3.5-flash"}) + College Knowledge Base"
        } else {
            "Offline College Knowledge Base"
        }

        // Save AI response to database
        val aiMsg = ChatMessageEntity(
            sender = "ai",
            text = ragResult.answer,
            sources = sourcesText
        )
        dao.insertChatMessage(aiMsg)

        return@withContext ragResult.answer
    }

    private suspend fun buildRagContext(query: String): Pair<String, String> {
        val notices = dao.getNoticesSnapshot()
        val courses = dao.getCoursesSnapshot()
        val faculty = dao.getFacultySnapshot()
        val faqs = dao.getFaqsSnapshot()
        val facilities = dao.getFacilitiesSnapshot()
        val exams = dao.getExamScheduleSnapshot()
        val placements = dao.getPlacementStatsSnapshot()
        val student = dao.getStudentProfile().firstOrNull()

        val sb = StringBuilder()
        val sourcesList = mutableListOf<String>()

        // 1. Personal student information
        if (query.contains("my") || query.contains("attendance") || query.contains("cgpa") || query.contains("profile") || query.contains("roll")) {
            student?.let {
                sb.appendLine("STUDENT PROFILE:")
                sb.appendLine("Name: ${it.name}, Roll: ${it.rollNo}, Dept: ${it.department}, Sem: ${it.semester}")
                sb.appendLine("Attendance: ${it.attendancePct}%, CGPA: ${it.cgpa}, Fee Status: ${it.feeDues}")
                sb.appendLine("Hostel Room: ${it.hostelRoom}, Faculty Mentor: ${it.mentor}")
                sb.appendLine()
                sourcesList.add("Student Record")
            }
        }

        // 2. Notices
        val relevantNotices = notices.filter { notice ->
            query.contains("notice") || query.contains("announcement") ||
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

        // 3. Courses
        val relevantCourses = courses.filter { c ->
            query.contains("course") || query.contains("program") || query.contains("degree") ||
                    query.contains(c.name.lowercase(Locale.ROOT)) || query.contains(c.degree.lowercase(Locale.ROOT)) ||
                    query.contains(c.department.lowercase(Locale.ROOT)) || query.contains(c.code.lowercase(Locale.ROOT))
        }
        if (relevantCourses.isNotEmpty() || query.contains("course") || query.contains("program")) {
            sb.appendLine("ACADEMIC PROGRAMS & COURSES:")
            val list = if (relevantCourses.isNotEmpty()) relevantCourses else courses
            list.forEach { c ->
                sb.appendLine("• ${c.name} (${c.degree}, Code: ${c.code}): Duration ${c.duration}, Annual Fee: ${c.feesPerYear}, Intake: ${c.intake}. Eligibility: ${c.eligibility}. Summary: ${c.description}")
            }
            sb.appendLine()
            sourcesList.add("Course Directory")
        }

        // 4. Faculty
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

        // 5. Exam Schedules
        if (query.contains("exam") || query.contains("timetable") || query.contains("schedule") || query.contains("date") || query.contains("test")) {
            sb.appendLine("UPCOMING EXAMINATION SCHEDULE:")
            exams.forEach { e ->
                sb.appendLine("• ${e.subjectCode} - ${e.subjectName}: ${e.date} (${e.time}) at ${e.venue}")
            }
            sb.appendLine()
            sourcesList.add("Exam Controller")
        }

        // 6. Placements
        if (query.contains("placement") || query.contains("package") || query.contains("recruit") || query.contains("salary") || query.contains("job") || query.contains("google")) {
            sb.appendLine("PLACEMENT STATISTICS:")
            placements.forEach { p ->
                sb.appendLine("• Year ${p.year}: Placement Rate: ${p.placementRate}, Highest: ${p.highestPackage}, Average: ${p.averagePackage}, Top Recruiters: ${p.topRecruiters}")
            }
            sb.appendLine()
            sourcesList.add("Placement Cell")
        }

        // 7. FAQs
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

        // 8. Facilities & Campus Resources
        val resources = dao.getCampusResourcesSnapshot()
        val matchedResources = resources.filter { r ->
            query.contains("resource") || query.contains("portal") || query.contains("lab") ||
                    query.contains("hpc") || query.contains("gpu") || query.contains("transit") ||
                    query.contains("shuttle") || query.contains("bus") || query.contains("health") ||
                    query.contains("clinic") || query.contains("counseling") || query.contains("doctor") ||
                    query.contains("maker") || query.contains("3d") || query.contains("sports") ||
                    query.contains("gym") || query.contains("pool") ||
                    query.contains(r.title.lowercase(Locale.ROOT)) ||
                    query.contains(r.category.lowercase(Locale.ROOT))
        }
        if (matchedResources.isNotEmpty() || query.contains("resource")) {
            sb.appendLine("CAMPUS RESOURCES & FACILITIES:")
            val resList = if (matchedResources.isNotEmpty()) matchedResources else resources
            resList.forEach { r ->
                sb.appendLine("• ${r.title} (${r.category}): Location: ${r.location}, Availability: ${r.availability}, Access: ${r.accessDetails}, Contact: ${r.contactInfo}. Info: ${r.description}")
            }
            sb.appendLine()
            sourcesList.add("Campus Resources & Services")
        }

        if (query.contains("facility") || query.contains("facilities") || query.contains("canteen") || query.contains("cafeteria")) {
            sb.appendLine("ADDITIONAL CAMPUS FACILITIES:")
            facilities.forEach { fc ->
                sb.appendLine("• ${fc.name} (${fc.category}): Location: ${fc.location}, Timings: ${fc.timings}, Rules: ${fc.rules}")
            }
            sb.appendLine()
            sourcesList.add("Campus Facilities")
        }

        return Pair(sb.toString(), sourcesList.joinToString(", "))
    }

    /**
     * Deterministic, high-accuracy offline fallback engine
     */
    private suspend fun generateOfflineRagResponse(query: String): String {
        val notices = dao.getNoticesSnapshot()
        val courses = dao.getCoursesSnapshot()
        val faculty = dao.getFacultySnapshot()
        val faqs = dao.getFaqsSnapshot()
        val resources = dao.getCampusResourcesSnapshot()
        val exams = dao.getExamScheduleSnapshot()
        val placements = dao.getPlacementStatsSnapshot()
        val student = dao.getStudentProfile().firstOrNull()

        // Check for personal student queries
        if (query.contains("my attendance") || query.contains("attendance")) {
            val att = student?.attendancePct ?: 85
            return "Your current cumulative attendance is **$att%**.\n\nSubject breakdown:\n• AI & Expert Systems: 95%\n• Cloud Computing: 88%\n• Database Systems: 78%\n• Network Security: 72% (Needs attention, min 75% required for exams)\n• Software Engineering: 92%\n\nRemember, the college mandates at least 75% in each subject to sit for mid-semester exams!"
        }

        if (query.contains("my fee") || query.contains("fee due") || query.contains("fee status")) {
            return "Your fee status for the current semester is: **${student?.feeDues ?: "Cleared (₹0)"}**.\n\nAll semester tuition and lab fees have been verified by the Finance Office. Next semester registration opens in June 2026."
        }

        if (query.contains("my profile") || query.contains("who am i") || query.contains("my roll")) {
            return "Here is your registered student profile:\n\n• **Name**: ${student?.name}\n• **Roll Number**: ${student?.rollNo}\n• **Department**: ${student?.department}\n• **Semester**: ${student?.semester}\n• **Current CGPA**: ${student?.cgpa}\n• **Hostel**: ${student?.hostelRoom}\n• **Academic Advisor**: ${student?.mentor}"
        }

        // Check for courses
        if (query.contains("course") || query.contains("program") || query.contains("branch") || query.contains("department")) {
            val list = courses.joinToString("\n\n") { c ->
                "• **${c.name}** (${c.degree})\n  Duration: ${c.duration} | Fees: ${c.feesPerYear}\n  Intake: ${c.intake} seats | Eligibility: ${c.eligibility}"
            }
            return "Here are the premier academic programs offered at CampusAI University:\n\n$list\n\nAdmissions are processed through national/state entrance counseling and merit quotas."
        }

        // Check for fees
        if (query.contains("fee") || query.contains("cost") || query.contains("tuition")) {
            return "Here is the annual fee structure for our core programs:\n\n• **B.Tech (CSE / AI & Data Science)**: ₹1,80,000 - ₹1,95,000 / year\n• **B.Tech (ECE / Mechanical)**: ₹1,40,000 - ₹1,60,000 / year\n• **MCA**: ₹1,20,000 / year\n• **MBA**: ₹1,65,000 / year\n• **Hostel & Food**: ₹85,000 / year\n\n*Note: Top 5% academic performers receive up to 40% merit scholarship waiver.*"
        }

        // Check for notices
        if (query.contains("notice") || query.contains("announcement") || query.contains("news") || query.contains("event")) {
            val list = notices.take(4).joinToString("\n\n") { n ->
                "📢 **${n.title}** (${n.date})\n*${n.department}* — ${n.content}"
            }
            return "Here are the latest notices published on the college board:\n\n$list"
        }

        // Check for exams
        if (query.contains("exam") || query.contains("schedule") || query.contains("timetable") || query.contains("test")) {
            val list = exams.joinToString("\n") { e ->
                "• **${e.subjectCode}**: ${e.subjectName}\n  Date: ${e.date} | Time: ${e.time} | Venue: ${e.venue}"
            }
            return "Here is your upcoming Mid-Semester Examination Schedule (Spring 2026):\n\n$list\n\n*Hall tickets will be verified at the entrance. Ensure minimum 75% attendance.*"
        }

        // Check for placements
        if (query.contains("placement") || query.contains("package") || query.contains("salary") || query.contains("job") || query.contains("recruit")) {
            val p = placements.firstOrNull()
            return "CampusAI University maintains an outstanding placement track record:\n\n• **Overall Placement Rate**: ${p?.placementRate ?: "94.2%"}\n• **Highest Package**: ${p?.highestPackage ?: "₹44 LPA (₹44,00,000 / year)"}\n• **Average Package**: ${p?.averagePackage ?: "₹8.5 LPA (₹8,50,000 / year)"}\n• **Top Recruiters**: ${p?.topRecruiters ?: "Google, Microsoft, Amazon, Cisco, Qualcomm"}\n\nThe Training & Placement Cell conducts regular mock interviews, aptitude training, and coding bootcamps for registered students."
        }

        // Check for library
        if (query.contains("library") || query.contains("book")) {
            return "📚 **Central Library Information**:\n\n• **Timings**: 8:00 AM – 10:00 PM (Extended to 11:30 PM during exam seasons)\n• **Location**: Knowledge Tower, 1st & 2nd Floors\n• **Capacity**: Over 85,000 volumes, 24/7 Digital Hub with IEEE, ACM, Springer access\n• **Borrowing Policy**: Up to 5 books for 21 days with RFID Student ID card."
        }

        // Check for hostel
        if (query.contains("hostel") || query.contains("room") || query.contains("mess")) {
            return "🏠 **Hostel & Mess Amenities**:\n\n• Separate secure Boys and Girls residential complexes with 24/7 high-speed Wi-Fi\n• AC & Non-AC room choices with attached baths and study desks\n• 4-meal hygienic dining hall with continental and regional menus\n• Curfew timings: 9:30 PM (Weekdays), 10:30 PM (Weekends)\n• Warden Office: Dr. Henry Miller (North Campus Zone)."
        }

        // Check for faculty
        if (query.contains("faculty") || query.contains("hod") || query.contains("professor") || query.contains("advisor")) {
            val list = faculty.joinToString("\n\n") { f ->
                "• **${f.name}** — ${f.designation}\n  Dept: ${f.department} | Cabin: ${f.cabin}\n  Email: ${f.email} | Office Hours: ${f.officeHours}"
            }
            return "Here are key faculty members and advisors:\n\n$list"
        }

        // Check for campus resources & services
        val matchedResource = resources.firstOrNull { r ->
            val t = r.title.lowercase(Locale.ROOT)
            val c = r.category.lowercase(Locale.ROOT)
            query.contains(t) ||
                    (query.contains("shuttle") || query.contains("transit") || query.contains("bus")) && c.contains("transport") ||
                    (query.contains("gpu") || query.contains("hpc") || query.contains("cluster")) && c.contains("computing") ||
                    (query.contains("health") || query.contains("doctor") || query.contains("clinic") || query.contains("wellness") || query.contains("counseling")) && c.contains("health") ||
                    (query.contains("maker") || query.contains("3d print") || query.contains("prototype")) && c.contains("computing") ||
                    (query.contains("sport") || query.contains("pool") || query.contains("swim") || query.contains("badminton") || query.contains("court")) && c.contains("sports") ||
                    (query.contains("ieee") || query.contains("acm") || query.contains("journal") || query.contains("digital library")) && c.contains("digital")
        }
        if (matchedResource != null) {
            return "🏛️ **${matchedResource.title}** (${matchedResource.category})\n\n• **Location**: ${matchedResource.location}\n• **Availability**: ${matchedResource.availability}\n• **Access Details**: ${matchedResource.accessDetails}\n• **Contact & Inquiries**: ${matchedResource.contactInfo}\n\n${matchedResource.description}\n\n*(Source: Institutional Campus Resources Database)*"
        }

        // Check FAQ matching
        val faqMatch = faqs.firstOrNull { faq ->
            val words = query.split(" ").filter { it.length > 3 }
            words.any { faq.keywords.contains(it) || faq.question.lowercase(Locale.ROOT).contains(it) }
        }
        if (faqMatch != null) {
            return "${faqMatch.answer}\n\n*(Source: Official College Knowledge Base - ${faqMatch.category} Section)*"
        }

        return "I am your CampusAI 3D holographic college assistant. You can ask me about:\n\n• **Campus Resources**: Digital libraries, GPU clusters, shuttle buses, and health center\n• **Academic Programs**: Courses, eligibility, and annual fee structures\n• **Exams & Timetables**: Mid-term dates, hall tickets, and test venues\n• **Notices & Circulars**: Tech fest 'Innovate 2026', deadlines, and events\n• **Student Records**: Your personal attendance, CGPA, and mentor details\n• **Placements**: Recruiters, packages, and eligibility criteria\n• **Institutional FAQs**: Admissions, hostels, library borrowing, and policies."
    }
}
