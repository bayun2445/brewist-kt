package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.FirestoreBrewRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BrewRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveBrew_authenticatedUser_success() = runBlocking {
    signInTestUser("alice@example.com")
    val repository = FirestoreBrewRepository(firestore)

    val brewId = withTimeout(5000L) {
      repository.saveBrew(
        beanName = "Ethiopia Guji",
        recipeName = "Metode 4:6 (Tetsu Kasuya)",
        doseGrams = 15f,
        ratio = 15f,
        totalWaterGrams = 225,
        durationSeconds = 210,
        rating = 5,
        tastingNotes = "Peach, jasmine tea, honey"
      )
    }

    assertNotNull(brewId)
    assertTrue(brewId.isNotBlank())
  }

  @Test
  fun observeUserBrews_authenticatedOwner_emitsRecord() = runBlocking {
    signInTestUser("alice@example.com")
    val repository = FirestoreBrewRepository(firestore)

    val brewId = withTimeout(5000L) {
      repository.saveBrew(
        beanName = "Gayo Natural",
        recipeName = "Hoffmann V60",
        doseGrams = 15f,
        ratio = 16f,
        totalWaterGrams = 240,
        durationSeconds = 195,
        rating = 4,
        tastingNotes = "Chocolate, berries"
      )
    }

    val brews = withTimeout(5000L) {
      repository.observeUserBrews().first { list ->
        list.any { it.id == brewId }
      }
    }

    assertTrue(brews.any { it.id == brewId })
    val saved = brews.first { it.id == brewId }
    assertEquals("Gayo Natural", saved.beanName)
    assertEquals(15.0, saved.doseGrams, 0.001)
  }

  @Test
  fun unauthenticatedUser_throwsIllegalStateException() = runBlocking {
    auth.signOut()
    val repository = FirestoreBrewRepository(firestore)

    try {
      repository.requireUserId()
      fail("Expected IllegalStateException for unauthenticated user")
    } catch (e: IllegalStateException) {
      assertTrue(e.message?.contains("User must be signed in with Google") == true)
    }
  }

  @Test
  fun deleteBrew_authenticatedOwner_success() = runBlocking {
    signInTestUser("alice@example.com")
    val repository = FirestoreBrewRepository(firestore)

    val brewId = withTimeout(5000L) {
      repository.saveBrew(
        beanName = "To Delete",
        recipeName = "Test",
        doseGrams = 15f,
        ratio = 15f,
        totalWaterGrams = 225,
        durationSeconds = 180,
        rating = 3,
        tastingNotes = ""
      )
    }

    withTimeout(5000L) {
      repository.deleteBrew(brewId)
    }

    val brews = withTimeout(5000L) {
      repository.observeUserBrews().first { list ->
        list.none { it.id == brewId }
      }
    }
    assertTrue(brews.none { it.id == brewId })
  }
}
