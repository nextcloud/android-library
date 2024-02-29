/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017 Joas Schilling <coding@schilljs.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.activities.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PreviewList Parser
 */
public class PreviewObjectAdapter extends TypeAdapter<PreviewObject> {

    @Override
    public void write(JsonWriter out, PreviewObject value) {
        // not needed
    }

    @Override
    public PreviewObject read(JsonReader in) throws IOException {
        PreviewObject preview = new PreviewObject();
        in.beginObject();

        while (in.hasNext()) {
            String name = in.peek().toString();
            if (!name.isEmpty()) {
                preview = readObject(in);
            }
        }

        in.endObject();

        return preview;
    }

    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT",
            justification = "default case is not found correctly")
    private PreviewObject readObject(JsonReader in) throws IOException {
        String tag;
        PreviewObject preview = new PreviewObject();

        do {
            try {
                tag = in.nextName();
            } catch (IllegalStateException e) {
                in.skipValue();
                tag = "";
            }

            switch (tag) {
                case "source":
                    preview.setSource(in.nextString());
                    break;

                case "link":
                    preview.setLink(in.nextString());
                    break;

                case "isMimeTypeIcon":
                    preview.setMimeTypeIcon(in.nextBoolean());
                    break;

                case "mimeType":
                    preview.setMimeType(in.nextString());
                    break;

                case "fileId":
                    preview.setFileId(in.nextInt());
                    break;

                case "view":
                    preview.setView(in.nextString());
                    break;

                case "filename":
                    preview.setFilename(in.nextString());

                default:
                    // do nothing
                    break;
            }
        } while (in.hasNext());

        return preview;
    }
}

