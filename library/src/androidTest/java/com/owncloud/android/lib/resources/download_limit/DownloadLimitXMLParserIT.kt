/* Nextcloud Android Library is available under MIT license
 *
 *   @author TSI-mc
 *   Copyright (C) 2023 TSI-mc
 *   Copyright (C) 2023 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.download_limit

import org.junit.Assert.*
import org.junit.Test

class DownloadLimitXMLParserIT {

    @Test
    fun parseSuccessResponseWithValues(){
        val xml = """<?xml version="1.0"?>
            <ocs> 
              <meta>
                <status>ok</status> 
                <statuscode>200</statuscode> 
                <message>OK</message> 
              </meta> 
              <data> 
                 <limit>5</limit> 
                 <count>1</count> 
              </data> 
            </ocs>
            """

        val remoteOperationResult = DownloadLimitXMLParser().parse(true, xml)

        assertTrue(remoteOperationResult.isSuccess)
        assertEquals(5, remoteOperationResult.resultData.limit)
        assertEquals(1, remoteOperationResult.resultData.count)
    }


    @Test
    fun parseSuccessResponseWithNoValues(){
        val xml = """<?xml version="1.0"?>
             <ocs>
              <meta>
               <status>ok</status>
               <statuscode>200</statuscode>
               <message>OK</message>
              </meta>
              <data>
                <limit/>
                <count/>
              </data>
             </ocs>
            """

        val remoteOperationResult = DownloadLimitXMLParser().parse(true, xml)

        assertTrue(remoteOperationResult.isSuccess)
        assertEquals(0, remoteOperationResult.resultData.limit)
        assertEquals(0, remoteOperationResult.resultData.count)
    }

    @Test
    fun parseSuccessResponseForUpdateDeleteOperations(){
        val xml = """<?xml version="1.0"?>
             <ocs>
              <meta>
                <status>ok</status>
                <statuscode>200</statuscode>
                <message>OK</message>
               </meta>
               <data/>
             </ocs>
            """

        val remoteOperationResult = DownloadLimitXMLParser().parse(true, xml)

        assertTrue(remoteOperationResult.isSuccess)
    }

    @Test
    fun parseFailResponse(){
        val xml = """<?xml version="1.0"?>
             <ocs>
              <meta>
                <status>ok</status>
                <statuscode>403</statuscode>
                <message>OK</message>
               </meta>
               <data/>
             </ocs>
            """

        val remoteOperationResult = DownloadLimitXMLParser().parse(true, xml)

        assertFalse(remoteOperationResult.isSuccess)
    }

}