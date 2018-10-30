/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Joas Schilling
 *
 *   @author Joas Schilling
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
package com.owncloud.android.lib.resources.activities.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

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

    private PreviewObject readObject(JsonReader in) throws IOException {
        String tag;
        PreviewObject preview = new PreviewObject();

        do {
            tag = in.nextName();
            
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

                default:
                    // do nothing
                    break;
            }
        } while (in.hasNext());

        return preview;
    }
}

