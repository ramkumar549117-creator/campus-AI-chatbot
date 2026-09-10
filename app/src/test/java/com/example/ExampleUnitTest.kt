package com.example

import com.example.data.remote.GeminiCandidate
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateRequest
import com.example.data.remote.GeminiGenerateResponse
import com.example.data.remote.GeminiPart
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun geminiGenerateResponse_extractText_returnsFirstCandidateText() {
    val response = GeminiGenerateResponse(
      candidates = listOf(
        GeminiCandidate(
          content = GeminiContent(
            parts = listOf(GeminiPart(text = "The library closes at 9:00 PM."))
          )
        )
      )
    )

    assertEquals("The library closes at 9:00 PM.", response.extractText())
  }

  @Test
  fun geminiGenerateRequest_buildsProperStructure() {
    val request = GeminiGenerateRequest(
      contents = listOf(
        GeminiContent(
          role = "user",
          parts = listOf(GeminiPart(text = "What is the fee for B.Tech CSE?"))
        )
      ),
      systemInstruction = GeminiContent(
        parts = listOf(GeminiPart(text = "You are CampusAI."))
      )
    )

    assertEquals(1, request.contents.size)
    assertEquals("What is the fee for B.Tech CSE?", request.contents.first().parts.first().text)
    assertEquals("You are CampusAI.", request.systemInstruction?.parts?.first()?.text)
  }

  @Test
  fun faqEntity_canBeCreatedAndUpdated() {
    val faq = com.example.data.local.FaqEntity(
      id = 1,
      question = "What are the library hours?",
      answer = "Central Library is open from 8:00 AM to 10:00 PM.",
      category = "Facilities",
      keywords = "library books timing study"
    )

    assertEquals(1, faq.id)
    assertEquals("Facilities", faq.category)

    val updated = faq.copy(answer = "Central Library is open from 8:00 AM to 11:00 PM during exams.")
    assertEquals("Central Library is open from 8:00 AM to 11:00 PM during exams.", updated.answer)
  }
}
