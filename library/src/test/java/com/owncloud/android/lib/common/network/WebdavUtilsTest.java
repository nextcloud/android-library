/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class WebdavUtilsTest {

    @Test
    public void parseResponseDate_parsesImfFixdate() {
        Date result = WebdavUtils.parseResponseDate(
                "Wed, 09 Sep 2026 08:49:37 GMT"
        );

        assertNotNull(result);
        assertEquals(
                Instant.parse("2026-09-09T08:49:37Z"),
                result.toInstant()
        );
    }

    @Test
    public void parseResponseDate_parsesRfc850Date() {
        Date result = WebdavUtils.parseResponseDate(
                "Sunday, 06-Nov-94 08:49:37 GMT"
        );

        assertNotNull(result);
        assertEquals(
                Instant.parse("1994-11-06T08:49:37Z"),
                result.toInstant()
        );
    }

    @Test
    public void parseResponseDate_parsesAsctimeDateWithSingleDigitDay() {
        Date result = WebdavUtils.parseResponseDate(
                "Sun Nov  6 08:49:37 1994"
        );

        assertNotNull(result);
        assertEquals(
                Instant.parse("1994-11-06T08:49:37Z"),
                result.toInstant()
        );
    }

    @Test
    public void parseResponseDate_parsesAsctimeDateWithTwoDigitDay() {
        Date result = WebdavUtils.parseResponseDate(
                "Sun Nov 16 08:49:37 1994"
        );

        assertNotNull(result);
        assertEquals(
                Instant.parse("1994-11-16T08:49:37Z"),
                result.toInstant()
        );
    }

    @Test
    public void parseResponseDate_rejectsInvalidWeekday() {
        Date result = WebdavUtils.parseResponseDate(
                "Monday, 06-Nov-94 08:49:37 GMT"
        );

        assertNull(result);
    }

    @Test
    public void parseResponseDate_rejectsInvalidDate() {
        Date result = WebdavUtils.parseResponseDate(
                "Wed, 31 Feb 2026 08:49:37 GMT"
        );

        assertNull(result);
    }

    @Test
    public void parseResponseDate_rejectsTrailingCharacters() {
        Date result = WebdavUtils.parseResponseDate(
                "Wed, 09 Sep 2026 08:49:37 GMT trailing"
        );

        assertNull(result);
    }

    @Test
    public void parseResponseDate_returnsNullForNullAndEmptyInput() {
        assertNull(WebdavUtils.parseResponseDate(null));
        assertNull(WebdavUtils.parseResponseDate(""));
    }

    @Test
    public void parseShareExpirationDate_parsesShareApiFormat() {
        String value = "2026-09-10 23:59:59";

        Date result = WebdavUtils.parseShareExpirationDate(value);

        assertNotNull(result);
        assertEquals(
                LocalDateTime.parse(value)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                result.toInstant()
        );
    }

    @Test
    public void parseShareExpirationDate_parses24HourTime() {
        String value = "2026-09-10 23:00:00";

        Date result = WebdavUtils.parseShareExpirationDate(value);

        assertNotNull(result);
        assertEquals(
                LocalDateTime.parse(value)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                result.toInstant()
        );
    }

    @Test
    public void parseShareExpirationDate_rejectsInvalidDate() {
        Date result = WebdavUtils.parseShareExpirationDate(
                "2026-02-29 12:00:00"
        );

        assertNull(result);
    }

    @Test
    public void parseShareExpirationDate_rejectsHttpDate() {
        Date result = WebdavUtils.parseShareExpirationDate(
                "Wed, 09 Sep 2026 08:49:37 GMT"
        );

        assertNull(result);
    }

    @Test
    public void parseShareExpirationDate_returnsNullForNullAndEmptyInput() {
        assertNull(WebdavUtils.parseShareExpirationDate(null));
        assertNull(WebdavUtils.parseShareExpirationDate(""));
    }
}
