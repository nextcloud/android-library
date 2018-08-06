/**
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.lib.common;

import org.parceler.Parcel;

/**
 * Quota data model
 */

@Parcel
public class ExternalLink {
    public Integer id;
    public String iconUrl;
    public String language;
    public ExternalLinkType type;
    public String name;
    public String url;
    public boolean redirect;

    public ExternalLink() {

    }

    public ExternalLink(Integer id, String iconUrl, String language, ExternalLinkType type, String name, String url, 
                        boolean redirect) {
        this.id = id;
        this.iconUrl = iconUrl;
        this.language = language;
        this.type = type;
        this.name = name;
        this.url = url;
        this.redirect = redirect;
    }
}

