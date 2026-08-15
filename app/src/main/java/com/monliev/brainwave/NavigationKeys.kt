package com.monliev.brainwave

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Onboarding : NavKey

@Serializable
data object Main : NavKey

@Serializable
data class CategoryDetail(val categoryName: String) : NavKey

@Serializable
data class Player(val presetId: String) : NavKey

@Serializable
data object Statistics : NavKey
