/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.sampleclient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.owncloud.android.lib.resources.files.model.RemoteFile;

public class FilesArrayAdapter extends ArrayAdapter<RemoteFile> {
	
	public FilesArrayAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = (TextView)super.getView(position, convertView, parent);
	    textView.setText(getItem(position).getRemotePath());
	    return textView;
	}		
}

