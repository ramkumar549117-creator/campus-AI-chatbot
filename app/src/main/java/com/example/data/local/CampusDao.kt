package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusDao {

    // Notices
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, id DESC")
    fun getAllNotices(): Flow<List<CollegeNoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: CollegeNoticeEntity): Long

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Int)

    // Courses
    @Query("SELECT * FROM courses ORDER BY department, name")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: Int)

    // Faculty
    @Query("SELECT * FROM faculty ORDER BY department, name")
    fun getAllFaculty(): Flow<List<FacultyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacultyList(facultyList: List<FacultyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: FacultyEntity): Long

    @Query("DELETE FROM faculty WHERE id = :id")
    suspend fun deleteFaculty(id: Int)

    // FAQs
    @Query("SELECT * FROM faqs ORDER BY category")
    fun getAllFaqs(): Flow<List<FaqEntity>>

    @Query("SELECT * FROM faqs WHERE category = :category ORDER BY id ASC")
    fun getFaqsByCategory(category: String): Flow<List<FaqEntity>>

    @Query("SELECT * FROM faqs WHERE question LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%' OR answer LIKE '%' || :query || '%'")
    fun searchFaqs(query: String): Flow<List<FaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaqs(faqs: List<FaqEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqEntity): Long

    @Update
    suspend fun updateFaq(faq: FaqEntity)

    @Query("DELETE FROM faqs WHERE id = :id")
    suspend fun deleteFaq(id: Int)

    @Query("DELETE FROM faqs")
    suspend fun clearFaqs()

    // Campus Resources
    @Query("SELECT * FROM campus_resources ORDER BY category, title")
    fun getAllCampusResources(): Flow<List<CampusResourceEntity>>

    @Query("SELECT * FROM campus_resources WHERE category = :category ORDER BY title ASC")
    fun getCampusResourcesByCategory(category: String): Flow<List<CampusResourceEntity>>

    @Query("SELECT * FROM campus_resources WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchCampusResources(query: String): Flow<List<CampusResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampusResources(resources: List<CampusResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampusResource(resource: CampusResourceEntity): Long

    @Update
    suspend fun updateCampusResource(resource: CampusResourceEntity)

    @Query("DELETE FROM campus_resources WHERE id = :id")
    suspend fun deleteCampusResource(id: Int)

    @Query("DELETE FROM campus_resources")
    suspend fun clearCampusResources()

    // Student Information & Profile
    @Query("SELECT * FROM student_profile LIMIT 1")
    fun getStudentProfile(): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM student_profile LIMIT 1")
    suspend fun getStudentProfileSnapshot(): StudentProfileEntity?

    @Query("SELECT * FROM student_profile WHERE rollNo = :rollNo LIMIT 1")
    fun getStudentByRollNo(rollNo: String): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM student_profile ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<StudentProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProfile(profile: StudentProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentProfileEntity>)

    @Update
    suspend fun updateStudentProfile(profile: StudentProfileEntity)

    @Query("DELETE FROM student_profile WHERE rollNo = :rollNo")
    suspend fun deleteStudentProfile(rollNo: String)

    // Exam Schedule
    @Query("SELECT * FROM exam_schedule ORDER BY date ASC")
    fun getExamSchedule(): Flow<List<ExamScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamSchedule(schedules: List<ExamScheduleEntity>)

    // Placement Stats
    @Query("SELECT * FROM placement_stats ORDER BY year DESC")
    fun getPlacementStats(): Flow<List<PlacementStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacementStats(stats: List<PlacementStatEntity>)

    // Facilities
    @Query("SELECT * FROM facilities")
    fun getAllFacilities(): Flow<List<CollegeFacilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<CollegeFacilityEntity>)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // RAG Search queries for Knowledge Base retrieval
    @Query("SELECT * FROM notices")
    suspend fun getNoticesSnapshot(): List<CollegeNoticeEntity>

    @Query("SELECT * FROM courses")
    suspend fun getCoursesSnapshot(): List<CourseEntity>

    @Query("SELECT * FROM faculty")
    suspend fun getFacultySnapshot(): List<FacultyEntity>

    @Query("SELECT * FROM faqs")
    suspend fun getFaqsSnapshot(): List<FaqEntity>

    @Query("SELECT * FROM campus_resources")
    suspend fun getCampusResourcesSnapshot(): List<CampusResourceEntity>

    @Query("SELECT * FROM facilities")
    suspend fun getFacilitiesSnapshot(): List<CollegeFacilityEntity>

    @Query("SELECT * FROM exam_schedule")
    suspend fun getExamScheduleSnapshot(): List<ExamScheduleEntity>

    @Query("SELECT * FROM placement_stats")
    suspend fun getPlacementStatsSnapshot(): List<PlacementStatEntity>
}
