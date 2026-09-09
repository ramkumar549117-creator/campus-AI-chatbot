package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InitialDataSeeder {

    suspend fun seedDatabaseIfEmpty(dao: CampusDao) = withContext(Dispatchers.IO) {
        val existingNotices = dao.getNoticesSnapshot()
        if (existingNotices.isNotEmpty()) {
            // Ensure any existing database with dollar signs is migrated to Rupees
            val student = dao.getStudentProfileSnapshot()
            if (student != null && student.feeDues.contains("$")) {
                dao.updateStudentProfile(student.copy(feeDues = student.feeDues.replace("$0", "₹0").replace("$", "₹")))
            }

            val existingCourses = dao.getCoursesSnapshot()
            for (course in existingCourses) {
                if (course.feesPerYear.contains("$")) {
                    val rupeeFee = when {
                        course.code.contains("CS") -> "₹1,80,000 / year"
                        course.code.contains("AI") -> "₹1,95,000 / year"
                        course.code.contains("EC") -> "₹1,60,000 / year"
                        course.code.contains("ME") -> "₹1,40,000 / year"
                        course.code.contains("MC") -> "₹1,20,000 / year"
                        course.code.contains("MB") -> "₹1,65,000 / year"
                        else -> course.feesPerYear.replace("$", "₹")
                    }
                    dao.insertCourse(course.copy(feesPerYear = rupeeFee))
                }
            }

            val existingFaqs = dao.getFaqsSnapshot()
            for (faq in existingFaqs) {
                if (faq.answer.contains("$")) {
                    val updated = faq.answer
                        .replace("$5,200 to $7,000", "₹1,40,000 to ₹1,95,000")
                        .replace("$6,500-$7,000", "₹1,80,000 - ₹1,95,000")
                        .replace("$5,800", "₹1,60,000")
                        .replace("$5,200", "₹1,40,000")
                        .replace("$2,200", "₹85,000")
                        .replace("$44,000 / annum (44 LPA)", "₹44 LPA (₹44,00,000 / annum)")
                        .replace("$8,500 / annum (8.5 LPA)", "₹8.5 LPA (₹8,50,000 / annum)")
                        .replace("$", "₹")
                    dao.insertFaq(faq.copy(answer = updated))
                }
            }

            val existingPlacements = dao.getPlacementStatsSnapshot()
            for (stat in existingPlacements) {
                if (stat.highestPackage.contains("$") || stat.averagePackage.contains("$")) {
                    dao.insertPlacementStats(listOf(
                        stat.copy(
                            highestPackage = stat.highestPackage.replace("$44,000", "₹44,00,000").replace("$42,000", "₹42,00,000").replace("$", "₹"),
                            averagePackage = stat.averagePackage.replace("$8,500", "₹8,50,000").replace("$8,100", "₹8,10,000").replace("$", "₹")
                        )
                    ))
                }
            }

            for (notice in existingNotices) {
                if (notice.content.contains("$")) {
                    val updated = notice.content
                        .replace("$15,000", "₹10,00,000 (10 Lakhs)")
                        .replace("$", "₹")
                    dao.insertNotice(notice.copy(content = updated))
                }
            }
            return@withContext
        }

        // 1. Seed Student Profile
        dao.insertStudentProfile(
            StudentProfileEntity(
                rollNo = "CS2023089",
                name = "Alex Rivera",
                email = "alex.rivera@campus.edu",
                department = "Computer Science & Engineering",
                semester = "6th Semester",
                cgpa = 8.84,
                attendancePct = 85,
                feeDues = "Cleared (₹0)",
                hostelRoom = "Block B - Room 304",
                mentor = "Dr. Sarah Mitchell (Cabin CS-204)"
            )
        )

        // 2. Seed Notices
        val notices = listOf(
            CollegeNoticeEntity(
                title = "Mid-Semester Examination Schedule - Spring 2026",
                category = "Exam",
                department = "Examination Cell",
                date = "2026-03-15",
                content = "Mid-semester theory and practical exams commence on March 25, 2026. Hall tickets available in the student portal from March 20. Minimum 75% attendance is strictly enforced to sit for exams.",
                isPinned = true
            ),
            CollegeNoticeEntity(
                title = "Annual Tech Fest 'Innovate 2026' Registrations Open",
                category = "Event",
                department = "Student Affairs",
                date = "2026-03-12",
                content = "Innovate 2026 will feature a 36-hour National Hackathon, Robotics Arena, and AI Paper Presentations. Prize pool: ₹10,00,000 (10 Lakhs). Register team before March 22.",
                isPinned = true
            ),
            CollegeNoticeEntity(
                title = "Google & Microsoft Campus Placement Drive",
                category = "Placement",
                department = "Training & Placement Cell",
                date = "2026-03-10",
                content = "Final & pre-final year students registered for T&P with CGPA >= 7.5 are eligible. Online coding assessments start March 28 on HackerRank.",
                isPinned = false
            ),
            CollegeNoticeEntity(
                title = "Semester Fee Payment Deadline Extended",
                category = "Academic",
                department = "Finance Office",
                date = "2026-03-05",
                content = "Last date for installment 2 tuition and hostel fee payment has been extended to March 31 without late surcharge. Pay via net banking or campus finance counter.",
                isPinned = false
            ),
            CollegeNoticeEntity(
                title = "Central Library Extended Hours During Exam Season",
                category = "Academic",
                department = "Library Administration",
                date = "2026-03-01",
                content = "Starting March 15, the central library and digital reading labs will stay open until 11:30 PM on weekdays and 9:00 PM on weekends. RFID cards required.",
                isPinned = false
            )
        )
        notices.forEach { dao.insertNotice(it) }

        // 3. Seed Courses
        val courses = listOf(
            CourseEntity(
                code = "CS101",
                name = "B.Tech in Computer Science & Engineering",
                department = "Computer Science",
                degree = "B.Tech",
                duration = "4 Years (8 Semesters)",
                feesPerYear = "₹1,80,000 / year",
                intake = 180,
                eligibility = "10+2 with Physics, Mathematics & Chemistry (Min 60%) or JEE / State Rank",
                description = "Focuses on Data Structures, Algorithms, Cloud Computing, Full-Stack Development, and Systems Architecture."
            ),
            CourseEntity(
                code = "AI201",
                name = "B.Tech in Artificial Intelligence & Data Science",
                department = "Computer Science & AI",
                degree = "B.Tech",
                duration = "4 Years (8 Semesters)",
                feesPerYear = "₹1,95,000 / year",
                intake = 120,
                eligibility = "10+2 with Physics, Mathematics & Computer/Chemistry (Min 65%)",
                description = "Specialized curriculum covering Machine Learning, Deep Learning, Natural Language Processing, Big Data Analytics, and Computer Vision."
            ),
            CourseEntity(
                code = "EC102",
                name = "B.Tech in Electronics & Communication Engineering",
                department = "Electronics & Communication",
                degree = "B.Tech",
                duration = "4 Years (8 Semesters)",
                feesPerYear = "₹1,60,000 / year",
                intake = 120,
                eligibility = "10+2 with Physics & Mathematics (Min 55%)",
                description = "Covers Embedded Systems, IoT, VLSI Design, Wireless Communication, and Signal Processing."
            ),
            CourseEntity(
                code = "ME103",
                name = "B.Tech in Mechanical & Robotics Engineering",
                department = "Mechanical Engineering",
                degree = "B.Tech",
                duration = "4 Years (8 Semesters)",
                feesPerYear = "₹1,40,000 / year",
                intake = 90,
                eligibility = "10+2 with PCM (Min 55%)",
                description = "Curriculum covers Thermal Engineering, CAD/CAM, Mechatronics, Industrial Automation, and 3D Prototyping."
            ),
            CourseEntity(
                code = "MC301",
                name = "Master of Computer Applications (MCA)",
                department = "Computer Applications",
                degree = "MCA",
                duration = "2 Years (4 Semesters)",
                feesPerYear = "₹1,20,000 / year",
                intake = 60,
                eligibility = "BCA, B.Sc Computer Science or equivalent with 55% marks",
                description = "Advanced Software Engineering, Enterprise Application Development, Cloud DevOps, and Cybersecurity."
            ),
            CourseEntity(
                code = "MB401",
                name = "Master of Business Administration (MBA)",
                department = "Management Studies",
                degree = "MBA",
                duration = "2 Years (4 Semesters)",
                feesPerYear = "₹1,65,000 / year",
                intake = 90,
                eligibility = "Bachelor's degree in any discipline with CAT / MAT score",
                description = "Specializations in Business Analytics, Marketing, Human Resources, and FinTech Management."
            )
        )
        dao.insertCourses(courses)

        // 4. Seed Faculty
        val faculty = listOf(
            FacultyEntity(
                name = "Dr. Sarah Mitchell",
                department = "Computer Science & Engineering",
                designation = "Professor & Head of Department",
                qualification = "Ph.D. in Computer Science (MIT), M.Tech (Stanford)",
                email = "s.mitchell@campus.edu",
                cabin = "CS-Block, Room 204",
                officeHours = "Mon & Wed: 2:00 PM - 4:00 PM"
            ),
            FacultyEntity(
                name = "Prof. David Chen",
                department = "Artificial Intelligence & Data Science",
                designation = "Associate Professor",
                qualification = "Ph.D. in Machine Learning (CMU)",
                email = "d.chen@campus.edu",
                cabin = "AI-Hub, Room 102",
                officeHours = "Tue & Thu: 11:00 AM - 1:00 PM"
            ),
            FacultyEntity(
                name = "Dr. Elena Rostova",
                department = "Electronics & Communication",
                designation = "Professor",
                qualification = "Ph.D. in VLSI & Nanotechnology",
                email = "e.rostova@campus.edu",
                cabin = "EC-Block, Room 310",
                officeHours = "Mon & Fri: 10:00 AM - 12:00 PM"
            ),
            FacultyEntity(
                name = "Prof. Marcus Vance",
                department = "Training & Placement Cell",
                designation = "Director of Career Services",
                qualification = "MBA (Harvard), B.Tech (CSE)",
                email = "placements@campus.edu",
                cabin = "Placement Cell, Admin Wing B",
                officeHours = "Daily: 3:00 PM - 5:00 PM"
            )
        )
        dao.insertFacultyList(faculty)

        // 5. Seed FAQs
        val faqs = listOf(
            FaqEntity(
                question = "What are the college admission requirements and procedure?",
                answer = "Admissions are merit-based through entrance examination ranks (JEE / State CET) and management quota. Prospective students must submit 10+2 transcripts, ID proof, and register through the college admissions portal. Counseling begins in June each year.",
                category = "Admission",
                keywords = "admission apply process cutoff eligibility counseling enroll"
            ),
            FaqEntity(
                question = "What is the total fee structure for B.Tech programs?",
                answer = "B.Tech tuition fee ranges from ₹1,40,000 to ₹1,95,000 per academic year depending on the specialization (CSE & AI: ₹1,80,000 - ₹1,95,000, ECE: ₹1,60,000, Mech: ₹1,40,000). Hostel and mess fee is ₹85,000 per year. Merit-based scholarships up to 40% are awarded to top 5% students.",
                category = "Fees",
                keywords = "fees cost tuition payment installment scholarship structure expense"
            ),
            FaqEntity(
                question = "What is the mandatory attendance policy for exams?",
                answer = "Students must maintain a minimum of 75% attendance in each individual subject to be eligible for end-semester exams. Between 65% and 74%, medical leave condonation is considered with Dean approval. Below 65%, student must repeat the course.",
                category = "Academic",
                keywords = "attendance percentage mandatory rule minimum leave absent"
            ),
            FaqEntity(
                question = "What are the hostel and mess facilities on campus?",
                answer = "The campus provides separate Boys and Girls residential complexes with 24/7 high-speed Wi-Fi, air-conditioned and non-AC rooms (double and single occupancy), reading rooms, recreation room, gymnasium, and hygienic 4-meal buffet catering with continental and regional options.",
                category = "Hostel",
                keywords = "hostel room mess food accommodation living stay"
            ),
            FaqEntity(
                question = "What are the central library timings and borrowing rules?",
                answer = "The Central Library is open from 8:00 AM to 10:00 PM on weekdays (extended to 11:30 PM during exams). Students can borrow up to 5 books for 21 days with their RFID smart card. Over 85,000 print volumes and access to IEEE Xplore, ACM, and Springer journals.",
                category = "Library",
                keywords = "library books timing open borrow digital reading hours"
            ),
            FaqEntity(
                question = "What are the college placement statistics and top recruiters?",
                answer = "CampusAI University achieved a 94.2% placement record in 2025. Highest domestic package was ₹44 LPA (₹44,00,000 / annum), and the average package was ₹8.5 LPA (₹8,50,000 / annum). Top recruiters include Google, Microsoft, Amazon, Cisco, TCS, Infosys, Deloitte, and Qualcomm.",
                category = "Placement",
                keywords = "placement job salary package recruit company hire stats career"
            ),
            FaqEntity(
                question = "Where is the college located and what transport is available?",
                answer = "The 120-acre lush green campus is located on Innovation Boulevard, Tech City. College operates 32 air-conditioned shuttle buses covering all major city stops. Dedicated bicycle lanes and EV charging stations are available on campus.",
                category = "Facilities",
                keywords = "transport bus route location address reach campus shuttle"
            ),
            FaqEntity(
                question = "How can I contact my faculty mentor or academic advisor?",
                answer = "Each student is assigned a dedicated faculty advisor. You can view your mentor in your Student Profile (e.g., Dr. Sarah Mitchell, Cabin CS-204) and schedule appointments via the student portal or attend during open office hours.",
                category = "Faculty",
                keywords = "faculty mentor teacher contact meet cabin office hours advisor"
            )
        )
        dao.insertFaqs(faqs)

        // 6. Seed Exam Schedule
        val exams = listOf(
            ExamScheduleEntity(
                subjectCode = "CS601",
                subjectName = "Artificial Intelligence & Expert Systems",
                date = "2026-03-25",
                time = "10:00 AM - 01:00 PM",
                venue = "Exam Hall 1 - Block A",
                semester = "6th Sem"
            ),
            ExamScheduleEntity(
                subjectCode = "CS602",
                subjectName = "Cloud Computing & Distributed Systems",
                date = "2026-03-27",
                time = "10:00 AM - 01:00 PM",
                venue = "Exam Hall 1 - Block A",
                semester = "6th Sem"
            ),
            ExamScheduleEntity(
                subjectCode = "CS603",
                subjectName = "Database Management & Big Data",
                date = "2026-03-30",
                time = "02:00 PM - 05:00 PM",
                venue = "Exam Hall 2 - Block C",
                semester = "6th Sem"
            ),
            ExamScheduleEntity(
                subjectCode = "CS604",
                subjectName = "Software Engineering & Agile DevOps",
                date = "2026-04-02",
                time = "10:00 AM - 01:00 PM",
                venue = "Exam Hall 1 - Block A",
                semester = "6th Sem"
            ),
            ExamScheduleEntity(
                subjectCode = "CS605",
                subjectName = "Computer Network Security & Cryptography",
                date = "2026-04-06",
                time = "10:00 AM - 01:00 PM",
                venue = "Exam Hall 3 - Block B",
                semester = "6th Sem"
            )
        )
        dao.insertExamSchedule(exams)

        // 7. Seed Placement Stats
        val placementStats = listOf(
            PlacementStatEntity(
                year = "2025",
                highestPackage = "₹44 LPA (₹44,00,000 / year)",
                averagePackage = "₹8.5 LPA (₹8,50,000 / year)",
                placementRate = "94.2%",
                topRecruiters = "Google, Microsoft, Amazon, Cisco, Qualcomm, Deloitte, TCS Digital"
            ),
            PlacementStatEntity(
                year = "2024",
                highestPackage = "₹42 LPA (₹42,00,000 / year)",
                averagePackage = "₹8.1 LPA (₹8,10,000 / year)",
                placementRate = "92.8%",
                topRecruiters = "Amazon, Microsoft, Infosys, Wipro, Oracle, Goldman Sachs"
            )
        )
        dao.insertPlacementStats(placementStats)

        // 8. Seed Facilities
        val facilities = listOf(
            CollegeFacilityEntity(
                name = "Central Library & Digital Knowledge Hub",
                category = "Library",
                timings = "8:00 AM - 10:00 PM (Weekdays), 9:00 AM - 6:00 PM (Sundays)",
                location = "Knowledge Tower, 1st & 2nd Floors",
                rules = "Silence mandatory, RFID card entry, 5 books limit for 21 days",
                contactPerson = "Mr. Robert Taylor, Chief Librarian"
            ),
            CollegeFacilityEntity(
                name = "Student Residential Hostels (Boys & Girls)",
                category = "Hostel",
                timings = "Curfew: 9:30 PM (Weekdays), 10:30 PM (Weekends)",
                location = "North Campus Residential Zone",
                rules = "Biometric check-in, Visitor registration at reception, Strict anti-ragging campus",
                contactPerson = "Dr. Henry Miller, Chief Warden"
            ),
            CollegeFacilityEntity(
                name = "Sports & Fitness Pavilion",
                category = "Sports",
                timings = "6:00 AM - 9:00 AM & 4:30 PM - 8:30 PM",
                location = "South Grounds",
                rules = "Sports shoes required, Indoor badminton, basketball, Olympic-size pool, gym",
                contactPerson = "Coach Amanda Reed"
            ),
            CollegeFacilityEntity(
                name = "Central Multi-Cuisine Food Court",
                category = "Cafeteria",
                timings = "7:30 AM - 10:00 PM",
                location = "Student Activity Center (SAC), Ground Floor",
                rules = "Cashless UPI/Card transactions, hygienic nutritional quality certified",
                contactPerson = "Chef Pierre Laurent"
            )
        )
        dao.insertFacilities(facilities)

        // 9. Initial Greeting Message from AI
        dao.insertChatMessage(
            ChatMessageEntity(
                sender = "ai",
                text = "Hello Alex! I am CampusAI, your 3D holographic college assistant. How can I help you today with courses, notices, exam timetable, attendance, or fees?",
                sources = "Campus Knowledge Base"
            )
        )
    }
}
