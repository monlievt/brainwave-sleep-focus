package com.monliev.brainwave.ui.main

import android.app.Application
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MainScreenViewModelTest verifies that the MainScreenViewModel can be correctly
 * initialized using standard mocked Android components.
 */
class MainScreenViewModelTest {
    
    @Test
    fun testViewModelInitialization() {
        val app = mockk<Application>(relaxed = true)
        val assets = mockk<AssetManager>(relaxed = true)
        every { app.assets } returns assets
        every { assets.list("presets") } returns emptyArray()

        val viewModel = MainScreenViewModel(app)
        assertNotNull(viewModel)
        assertTrue(viewModel.allPresets.isEmpty())
    }
}
