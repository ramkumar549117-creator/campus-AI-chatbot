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
    val category: String, // "Admission", "Fees", "Hostel", "Exams", "Library", "Placement", "Academics", "Campus Life"
    val keywords: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Campus Resource entity representing institutional facilities, laboratories,
 * online digital research portals, student transit, health & wellness centers.
 */
@Entity(tableName = "campus_resources")
data class CampusResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Digital & Library", "Research & Computing", "Health & Wellness", "Transport & Commute", "Student Services", "Sports & Fitness"
    val location: String,
    val availability: String,
    val accessDetails: String,
    val contactInfo: String,
    val description: String,
    val iconType: String = "default"
)

/**
 * Student Information entity representing profile records, academic status,
 * fee standing, attendance tracking, and administrative details.
 */
@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val rollNo: String = "CS2023089",
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@campus.edu",
    val phone: String = "+91 98765 43210",
    val department: String = "Computer Science & Engineering",
    val semester: String = "6th Semester",
    val batchYear: String = "2023-2027",
    val cgpa: Double = 8.84,
    val attendancePct: Int = 85,
    val feeDues: String = "Cleared (₹0)",
    val hostelRoom: String = "Block B - Room 304",
    val mentor: String = "Dr. Sarah Mitchell",
    val bloodGroup: String = "O+",
    val emergencyContact: String = "+91 98765 01234 (Guardian)",
    val libraryCardNo: String = "LIB-CS-2023-089",
    val enrolledCoursesCount: Int = 6
)

typealias StudentInformationEntity = StudentProfileEntity

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
