/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Alejandro Bautista
 *
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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * RichElement Parser
 */

public class RichElementTypeAdapter extends TypeAdapter<RichElement> {

    @Override
    public void write(JsonWriter out, RichElement value) {
        // not needed, only read support
    }

    @Override
    public RichElement read(JsonReader in) throws IOException {
        RichElement richElement = new RichElement();
        in.beginArray();
        int count = 0;
        while (in.hasNext()) {
            if (count == 0) {
                richElement.setRichSubject(in.nextString());
            } else {
                JsonToken nextType = in.peek();

                switch (nextType) {
                    case BEGIN_OBJECT:
                        in.beginObject();
                        read(richElement, in);
                        in.endObject();
                        break;

                    case BEGIN_ARRAY:
                        in.beginArray();
                        in.endArray();
                        break;

                    default:
                        // do nothing
                        break;
                }

            }
            count++;
        }


        in.endArray();

        return richElement;
    }

    private void read(RichElement richElement, JsonReader in) throws IOException {
        while (in.hasNext()) {
            String name = in.nextName();
            if (name != null && !name.isEmpty()) {
                richElement.getRichObjectList().add(readObject(name,in));
            }
        }
    }

    private RichObject readObject(String tag, JsonReader in) throws IOException {
        in.beginObject();
        RichObject richObject = new RichObject();
        richObject.setTag(tag);
        while (in.hasNext()) {
            String name;
            try {
                name = in.nextName();
            } catch (IllegalStateException e) {
                name = "";
            }
            
            switch (name) {
                case "type":
                    richObject.setType(getNextString(in));
                    break;
                case "id":
                    richObject.setId(getNextString(in));
                    break;
                case "name":
                    richObject.setName(getNextString(in));
                    break;
                case "path":
                    richObject.setPath(getNextString(in));
                    break;
                case "link":
                    richObject.setLink(getNextString(in));
                    break;
                case "server":
                    richObject.setLink(getNextString(in));
                    break;
                default:
                    in.skipValue(); // ignore value
                    break;
            }
        }
        in.endObject();
        return richObject;
    }

    private String getNextString(JsonReader in) {
        try {
            return in.nextString();
        } catch (IllegalStateException | IOException e) {
            return "";
        }
    }
}

