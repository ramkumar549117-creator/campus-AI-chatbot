package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class CollegeNoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Exam", "Event", "Placement", "Academic", "Urgent"
    val department: String,
    val date: String,
    val content: String,
    val isPinned: Boolean = false
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val name: String,
    val department: String,
    val degree: String, // "B.Tech", "M.Tech", "MCA", "MBA", "Ph.D"
    val duration: String,
    val feesPerYear: String,
    val intake: Int,
    val eligibility: String,
    val description: String
)

@Entity(tableName = "faculty")
data class FacultyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val department: String,
    val designation: String, // "Professor & HOD", "Associate Professor", etc.
    val qualification: String,
    val email: String,
    val cabin: String,
    val officeHours: String
)

@Entity(tableName = "faqs")
data class FaqEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val category: String, // "Admission", "Fees", "Hostel", "Exams", "Library", "Placement"
    val keywords: String
)

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val rollNo: String = "CS2023089",
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@campus.edu",
    val department: String = "Computer Science & Engineering",
    val semester: String = "6th Semester",
    val cgpa: Double = 8.84,
    val attendancePct: Int = 85,
    val feeDues: String = "Cleared (Rs 0)",
    val hostelRoom: String = "Block B - Room 304",
    val mentor: String = "Dr. Sarah Mitchell"
)

@Entity(tableName = "exam_schedule")
data class ExamScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectCode: String,
    val subjectName: String,
    val date: String,
    val time: String,
    val venue: String,
    val semester: String
)

@Entity(tableName = "placement_stats")
data class PlacementStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: String,
    val highestPackage: String,
    val averagePackage: String,
    val placementRate: String,
    val topRecruiters: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val intent: String? = null,
    val isVoice: Boolean = false,
    val sources: String? = null
)

@Entity(tableName = "facilities")
data class CollegeFacilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Library", "Hostel", "Sports", "Labs", "Cafeteria"
    val timings: String,
    val location: String,
    val rules: String,
    val contactPerson: String
)
