package com.dreamteam.breakloop.model;

/**
 * How an achievement is unlocked. The app evaluates each rule with its own
 * data (streak, daily usage, focus sessions); the backend only serves them.
 */

public enum AchievementRuleType {
    FIRST_DAY_UNDER_LIMIT,
    STREAK_DAYS,
    FOCUS_SESSIONS_COMPLETED,
    DAYS_UNDER_80_IN_A_ROW
}