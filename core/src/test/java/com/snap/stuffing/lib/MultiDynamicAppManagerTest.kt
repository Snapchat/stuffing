package com.snap.stuffing.lib

import android.content.Context
import com.snap.framework.developer.BuildConfigInfo
import com.snap.stuffing.api.AppSwitchHook
import com.snap.stuffing.api.DynamicAppConfig
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock

private const val TEST_EVENT = "test_event"

class MultiDynamicAppManagerTest {

    private val context = mock(Context::class.java)

    @Test
    fun testEventInvocation() {
        var called = false
        val runnable = Runnable {
            called = true
        }
        val config = DynamicAppConfig(mapOf(TEST_EVENT to runnable))
        val subject = MultiDynamicAppManager(context, BuildConfigInfo(), "", config, AppSwitchHook { fromAppFamily, toAppFamily ->  })
        subject.onEvent(TEST_EVENT)

        Assert.assertTrue(called)
    }
}
