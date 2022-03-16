/*   Nextcloud Android Library is available under MIT license
 *
 *   Copyright (C) 2017 Alejandro Bautista
 *   @author Alejandro Bautista
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
package com.owncloud.android.lib.resources.activities.model;

import com.google.gson.annotations.SerializedName;
import com.owncloud.android.lib.resources.activities.models.PreviewObject;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Activity Data Model
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

    @SerializedName("activity_id")
    private int activityId;
    private Date datetime;
    // legacy purposes
    private Date date;
    private String app;
    private String type;
    private String user;
    @SerializedName("affecteduser")
    private String affectedUser;
    private String subject;
    private String message;
    private String icon;
    private String link;
    @SerializedName("object_type")
    private String objectType;
    @SerializedName("object_id")
    private String objectId;
    @SerializedName("object_name")
    private String objectName;
    private List<PreviewObject> previews;
    @SerializedName("subject_rich")
    private RichElement richSubjectElement;
}
