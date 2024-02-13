/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android

import android.util.Log
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * C&p from https://stackoverflow.com/questions/45635833/how-can-i-use-flakytest-annotation-now on 18.03.2020
 */
class RetryTestRule(val retryCount: Int = 1) : TestRule {
    companion object {
        private val TAG = RetryTestRule::class.java.simpleName
    }

    override fun apply(
        base: Statement,
        description: Description
    ): Statement {
        return statement(base, description)
    }

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
