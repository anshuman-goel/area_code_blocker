package com.aistudio.areacodeblocker.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.util.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AppVersionTest {
    @Test
    @Config(sdk = [32])
    fun name_returnsPackageVersion_onPreApi33() {
        assertInstalledPackageVersion()
    }

    @Test
    @Config(sdk = [33])
    fun name_returnsPackageVersion_onApi33() {
        assertInstalledPackageVersion()
    }

    private fun assertInstalledPackageVersion() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val expected = application.packageManager
            .getPackageInfo(application.packageName, 0)
            .versionName
            .orEmpty()

        assertFalse(expected.isBlank())
        assertEquals(expected, AppVersion.name(application))
    }
}
