package com.owncloud.android.lib.resources.shares;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SharedWithMe {
    private String ownerUID;
    private String ownerDisplayName;
    private String note;
}
