/*  Nextcloud Android Library is available under MIT license
 *
 *   @author Andy Scherzinger
 *   Copyright (C) 2017 Andy Scherzinger
 *   Copyright (C) 2017 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.notifications.models;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notification data model.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    /**
     * Unique identifier of the notification, can be used to dismiss a notification.
     */
    @SerializedName("notification_id")
    public int notificationId;

    /**
     * Name of the app that triggered the notification.
     */
    public String app;

    /**
     * User id of the user that receives the notification.
     */
    public String user;

    /**
     * Date and time when the notification was published.
     */
    public Date datetime;

    /**
     * Type of the object the notification is about, that can be used
     * to mark a notification as resolved.
     */
    @SerializedName("object_type")
    public String objectType;

    /**
     * ID of the object the notification is about, that can be used
     * to mark a notification as resolved.
     */
    @SerializedName("object_id")
    public String objectId;

    /**
     * Translated short subject that should be presented to the user.
     */
    public String subject;

    /**
     * (Optional) Translated subject string with placeholders (see Rich Object String).
     */
    public String subjectRich;

    /**
     * (Optional) Subject parameters for {@code subjectRich} (see Rich Object String).
     */
    public Map<String, RichObject> subjectRichParameters;

    /**
     * (Optional) Translated potentially longer message that should be presented to the user.
     */
    public String message;

    /**
     * (Optional) Translated message string with placeholders (see Rich Object String).
     */
    public String messageRich;

    /**
     * (Optional) Message parameters for messageRich (see Rich Object String).
     */
    public Map<String, RichObject> messageRichParameters;

    /**
     * (Optional) A link that should be followed when the subject/message is clicked.
     */
    public String link;

    /**
     * (Optional) A link to an icon that should be shown next to the notification..
     */
    public String icon;

    /**
     * (Optional) An array of action elements.
     */
    public Collection<Action> actions;
}
