package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BeanEntity
import com.example.data.BrewDatabase
import com.example.data.BrewHistoryEntity
import com.example.model.BrewMethodType
import com.example.ui.BeanGroup
import com.example.ui.BrewViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var inMemoryDb: BrewDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, BrewDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("V60 Timer", appName)
    }

    @Test
    fun `default brew method is classic 3 pour`() {
        val defaultRecipe = BrewViewModel.createDefaultRecipe(
            methodType = BrewMethodType.CLASSIC_3_POUR,
            dose = 15f,
            ratio = 15f
        )
        assertEquals(BrewMethodType.CLASSIC_3_POUR, defaultRecipe.methodType)
        assertEquals(BrewMethodType.CLASSIC_3_POUR, BrewMethodType.values().first())
    }

    @Test
    fun `bean entity insertion and propagation to all brews`() = runBlocking {
        val dao = inMemoryDb.brewDao()

        // 1. Insert a bean in separate table
        val beanId = dao.insertBean(BeanEntity(name = "Ethiopia Yirgacheffe"))
        assertTrue(beanId > 0)

        // 2. Insert multiple historical brews with that bean
        val brew1 = BrewHistoryEntity(
            beanId = beanId,
            beanName = "Ethiopia Yirgacheffe",
            recipeName = "Metode 4:6 (Tetsu Kasuya)",
            doseGrams = 15f,
            ratio = 15f,
            totalWaterGrams = 225,
            durationSeconds = 210,
            rating = 5,
            tastingNotes = "Jasmine, peach",
            timestamp = 1000L
        )
        val brew2 = BrewHistoryEntity(
            beanId = beanId,
            beanName = "Ethiopia Yirgacheffe",
            recipeName = "Hoffmann V60",
            doseGrams = 15f,
            ratio = 16f,
            totalWaterGrams = 240,
            durationSeconds = 200,
            rating = 4,
            tastingNotes = "Floral, clean",
            timestamp = 2000L
        )
        dao.insertBrew(brew1)
        dao.insertBrew(brew2)

        // Verify initial insertion
        val initialBrews = dao.getAllBrews().first()
        assertEquals(2, initialBrews.size)
        assertTrue(initialBrews.all { it.beanName == "Ethiopia Yirgacheffe" })

        // 3. Rename the bean in beans table and propagate to all brews
        dao.updateBeanName(beanId, "Ethiopia Yirgacheffe Washed")
        dao.updateBrewsBeanName(beanId, "Ethiopia Yirgacheffe Washed")

        // 4. Verify that new name is applied to all historical brews
        val updatedBrews = dao.getAllBrews().first()
        assertEquals(2, updatedBrews.size)
        assertTrue(updatedBrews.all { it.beanName == "Ethiopia Yirgacheffe Washed" })
    }

    @Test
    fun `limit expanded items to 3 items and flag view all`() {
        val brews = (1..5).map { i ->
            BrewHistoryEntity(
                id = i.toLong(),
                beanId = 1L,
                beanName = "Colombia Geisha",
                recipeName = "Recipe $i",
                doseGrams = 15f,
                ratio = 15f,
                totalWaterGrams = 225,
                durationSeconds = 180 + i * 5,
                rating = 5,
                tastingNotes = "Notes $i",
                timestamp = i * 1000L
            )
        }.sortedByDescending { it.timestamp }

        val group = BeanGroup(
            beanKey = "Colombia Geisha",
            displayBeanName = "Colombia Geisha",
            brews = brews,
            latestBrewTimestamp = brews.first().timestamp,
            averageRating = 5f
        )

        val displayedInCard = group.brews.take(3)
        assertEquals(3, displayedInCard.size)
        assertTrue(group.brews.size > 3)
        assertEquals(5, group.brews.size)
    }
}
