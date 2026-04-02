package com.example.sporttracker

enum class SportType(val ua: String, val emoji: String) {
    GYM("Зал", "🏋️"),
    FOOTBALL("Футбол", "⚽"),
    RUNNING("Біг", "🏃"),
    TABLE_TENNIS("Настільний теніс", "🏓"),
    TENNIS("Теніс", "🎾"),
    SWIMMING("Плавання", "🏊"),
    CYCLING("Велосипед", "🚴");
}
