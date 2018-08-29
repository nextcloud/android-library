package com.owncloud.android.util;

import android.support.test.runner.AndroidJUnit4;

import com.owncloud.android.lib.common.operations.ExceptionParser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tobi on 3/21/18.
 */

@RunWith(AndroidJUnit4.class)
public class ExceptionParserIT {

    @Test
    public void testVirusException() throws IOException, XmlPullParserException {
        String virusException = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<d:error xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\">\n" +
                "  <s:exception>OCA\\DAV\\Connector\\Sabre\\Exception\\UnsupportedMediaType" +
                "</s:exception>\n" +
                "  <s:message>Virus Eicar-Test-Signature is detected in the file. " +
                "Upload cannot be completed.</s:message>\n" +
                "</d:error>";

        InputStream is = new ByteArrayInputStream(virusException.getBytes());
        ExceptionParser xmlParser = new ExceptionParser(is);

        Assert.assertTrue(xmlParser.isVirusException());
        Assert.assertFalse(xmlParser.isInvalidCharacterException());
    }

    @Test
    public void testInvalidCharacterException() throws IOException, XmlPullParserException {
        String virusException = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<d:error xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\">\n" +
                "  <s:exception>OC\\Connector\\Sabre\\Exception\\InvalidPath</s:exception>\n" +
                "  <s:message>Wrong Path</s:message>\n" +
                "</d:error>";

        InputStream is = new ByteArrayInputStream(virusException.getBytes());
        ExceptionParser xmlParser = new ExceptionParser(is);

        Assert.assertTrue(xmlParser.isInvalidCharacterException());
        Assert.assertFalse(xmlParser.isVirusException());
    }

    @Test
    public void testInvalidCharacterUploadException() throws IOException, XmlPullParserException {
        String virusException = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<d:error xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\">\n" +
                "  <s:exception>OCP\\Files\\InvalidPathException</s:exception>\n" +
                "  <s:message>Wrong Path</s:message>\n" +
                "</d:error>";

        InputStream is = new ByteArrayInputStream(virusException.getBytes());
        ExceptionParser xmlParser = new ExceptionParser(is);

        Assert.assertTrue(xmlParser.isInvalidCharacterException());
        Assert.assertFalse(xmlParser.isVirusException());
    }

    @Test
    public void testEmptyString() throws IOException, XmlPullParserException {
        String emptyString = "";

        InputStream is = new ByteArrayInputStream(emptyString.getBytes());
        ExceptionParser xmlParser = new ExceptionParser(is);

        Assert.assertFalse(xmlParser.isVirusException());
        Assert.assertFalse(xmlParser.isInvalidCharacterException());
    }

    @Test
    public void testString() throws IOException, XmlPullParserException {
        String emptyString = "";

        InputStream is = new ByteArrayInputStream(emptyString.getBytes());
        ExceptionParser xmlParser = new ExceptionParser(is);

        Assert.assertFalse(xmlParser.isVirusException());
        Assert.assertFalse(xmlParser.isInvalidCharacterException());
    }
}
