/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android

import android.util.Log
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * C&p from https://stackoverflow.com/questions/45635833/how-can-i-use-flakytest-annotation-now on 18.03.2020
 */
class RetryTestRule(
    val retryCount: Int = 1
) : TestRule {
    companion object {
        private val TAG = RetryTestRule::class.java.simpleName
    }

    override fun apply(
        base: Statement,
        description: Description
    ): Statement = statement(base, description)

    @Suppress("TooGenericExceptionCaught") // and this exactly what we want here
    private fun statement(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                Log.e(TAG, "Evaluating ${description.methodName}")

                var caughtThrowable: Throwable? = null

                for (i in 0 until retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        Log.e(TAG, description.methodName + ": run " + (i + 1) + " failed")
                    }
                }

                Log.e(TAG, description.methodName + ": giving up after " + retryCount + " failures")
                if (caughtThrowable != null) {
                    throw caughtThrowable
                }
            }
        }
    }
}
