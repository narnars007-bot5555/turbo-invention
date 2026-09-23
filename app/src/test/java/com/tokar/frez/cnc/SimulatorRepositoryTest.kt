package com.tokar.frez.cnc

import com.tokar.frez.cnc.data.models.CncDatabase
import com.tokar.frez.cnc.data.models.Hotspot
import com.tokar.frez.cnc.data.models.Scenario
import com.tokar.frez.cnc.data.models.ScenarioStep
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class SimulatorRepositoryTest {

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun testCncDatabaseJsonParsing() {
        val jsonSample = """
        {
          "brands": [
            {
              "id": "fanuc",
              "name": "FANUC",
              "country": "Япония",
              "models": [
                {
                  "id": "fanuc_0i_tf",
                  "name": "Fanuc 0i-TF Plus",
                  "type": "Токарная обработка",
                  "image_panel": "panels/fanuc_0i_tf.jpg",
                  "hotspots": [
                    {
                      "id": "btn_mode_ref",
                      "title": "Режим REF / HOME",
                      "x_percent": 60.1,
                      "y_percent": 82.1,
                      "radius_dp": 22,
                      "description": "Режим выхода станка в физический ноль.",
                      "category": "Режимы работы"
                    }
                  ],
                  "scenarios": [
                    {
                      "id": "ref_return",
                      "title": "Выход станка в ноль (REF)",
                      "steps": [
                        {
                          "step": 1,
                          "instruction": "Нажмите и активируйте режим REF на пульте.",
                          "target_hotspot": "btn_mode_ref"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val parsed = jsonParser.decodeFromString<CncDatabase>(jsonSample)
        assertNotNull(parsed)
        assertEquals(1, parsed.brands.size)

        val brand = parsed.brands.first()
        assertEquals("fanuc", brand.id)
        assertEquals("FANUC", brand.name)

        val model = brand.models.first()
        assertEquals("fanuc_0i_tf", model.id)
        assertEquals(1, model.hotspots.size)

        val hotspot = model.hotspots.first()
        assertEquals("btn_mode_ref", hotspot.id)
        assertEquals(60.1f, hotspot.x_percent, 0.01f)
        assertEquals(82.1f, hotspot.y_percent, 0.01f)

        val scenario = model.scenarios.first()
        assertEquals("ref_return", scenario.id)
        assertEquals(1, scenario.steps.size)
        assertEquals("btn_mode_ref", scenario.steps.first().target_hotspot)
    }

    @Test
    fun testHotspotCoordinateCalculations() {
        val hotspot = Hotspot(
            id = "btn_cycle_start",
            title = "CYCLE START",
            x_percent = 50.0f,
            y_percent = 80.0f,
            description = "Start button",
            category = "Control"
        )

        val screenWidth = 1000f
        val screenHeight = 2000f

        val calculatedX = (hotspot.x_percent / 100f) * screenWidth
        val calculatedY = (hotspot.y_percent / 100f) * screenHeight

        assertEquals(500f, calculatedX, 0.1f)
        assertEquals(1600f, calculatedY, 0.1f)
    }

    @Test
    fun testScenarioStepProgression() {
        val step1 = ScenarioStep(1, "Press REF", "btn_mode_ref")
        val step2 = ScenarioStep(2, "Press CYCLE START", "btn_cycle_start")

        val scenario = Scenario("ref_zero", "Zero Return", listOf(step1, step2))
        assertEquals(2, scenario.steps.size)

        var currentStepIndex = 0
        val targetStep1 = scenario.steps[currentStepIndex].target_hotspot
        assertEquals("btn_mode_ref", targetStep1)

        // Simulate correct button tap on step 1
        val userClickedHotspotId = "btn_mode_ref"
        if (userClickedHotspotId == targetStep1) {
            currentStepIndex++
        }

        assertEquals(1, currentStepIndex)
        val targetStep2 = scenario.steps[currentStepIndex].target_hotspot
        assertEquals("btn_cycle_start", targetStep2)
    }
}
