package com.devsneha.ar.data

data class Drill(
    val id: Int,
    val name: String,
    val description: String,
    val imageEmoji: String,
    val tips: List<String>,
    val difficulty: String,
    val duration: String
)

object DrillRepository {
    val drills = listOf(
        Drill(
            id = 1,
            name = "Drill 1",
            description = "Basic formation drill focusing on proper team positioning and fundamental movement patterns. This drill helps establish spacing and coordination between team members.",
            imageEmoji = "🏃‍♂️",
            tips = listOf(
                "Maintain 3-meter spacing between markers",
                "Keep your head up and eyes on the field",
                "Practice smooth transitions between positions",
                "Focus on consistent communication"
            ),
            difficulty = "Beginner",
            duration = "15-20 minutes"
        ),
        Drill(
            id = 2,
            name = "Drill 2",
            description = "Advanced maneuvering drill combining speed, agility, and tactical awareness. This exercise challenges players with complex movement patterns.",
            imageEmoji = "⚡",
            tips = listOf(
                "Execute sharp cuts and direction changes",
                "Maintain balance during rapid movements",
                "Practice reading field conditions",
                "Work on explosive acceleration"
            ),
            difficulty = "Advanced",
            duration = "25-30 minutes"
        ),
        Drill(
            id = 3,
            name = "Drill 3",
            description = "Team coordination drill emphasizing synchronized movements and communication. Perfect for building teamwork and timing.",
            imageEmoji = "🤝",
            tips = listOf(
                "Use clear verbal and visual signals",
                "Time your movements with teammates",
                "Practice switching positions fluidly",
                "Develop trust and anticipation"
            ),
            difficulty = "Intermediate",
            duration = "20-25 minutes"
        )
    )

    fun getDrillById(id: Int): Drill? {
        return drills.find { it.id == id }
    }
}