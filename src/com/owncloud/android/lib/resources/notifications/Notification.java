/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Andy Scherzinger
 *   Copyright (C) 2017 Nextcloud GmbH
 *
 *   @author Andy Scherzinger
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

package com.owncloud.android.lib.resources.notifications;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Notification data model.
 */
public class Notification {
    /**
     * Unique identifier of the notification, can be used to dismiss a notification.
     */
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
    public String object_type;

    /**
     * ID of the object the notification is about, that can be used
     * to mark a notification as resolved.
     */
    public String object_id;

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

    public Notification() {
    }

    public Notification(int notificationId,
                        String app,
                        String user,
                        Date datetime,
                        String object_type,
                        String object_id,
                        String subject,
                        String subjectRich,
                        Map<String, RichObject> subjectRichParameters,
                        String message, String messageRich,
                        Map<String, RichObject> messageRichParameters,
                        String link,
                        String icon,
                        Collection<Action> actions) {
        this.notificationId = notificationId;
        this.app = app;
        this.user = user;
        this.datetime = datetime;
        this.object_type = object_type;
        this.object_id = object_id;
        this.subject = subject;
        this.subjectRich = subjectRich;
        this.subjectRichParameters = subjectRichParameters;
        this.message = message;
        this.messageRich = messageRich;
        this.messageRichParameters = messageRichParameters;
        this.link = link;
        this.icon = icon;
        this.actions = actions;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getObject_type() {
        return object_type;
    }

    public void setObject_type(String object_type) {
        this.object_type = object_type;
    }

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectRich() {
        return subjectRich;
    }

    public void setSubjectRich(String subjectRich) {
        this.subjectRich = subjectRich;
    }

    public Map<String, RichObject> getSubjectRichParameters() {
        return subjectRichParameters;
    }

    public void setSubjectRichParameters(Map<String, RichObject> subjectRichParameters) {
        this.subjectRichParameters = subjectRichParameters;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageRich() {
        return messageRich;
    }

    public void setMessageRich(String messageRich) {
        this.messageRich = messageRich;
    }

    public Map<String, RichObject> getMessageRichParameters() {
        return messageRichParameters;
    }

    public void setMessageRichParameters(Map<String, RichObject> messageRichParameters) {
        this.messageRichParameters = messageRichParameters;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Collection<Action> getActions() {
        return actions;
    }

    public void setActions(Collection<Action> actions) {
        this.actions = actions;
    }
}
