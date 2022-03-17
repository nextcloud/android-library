package com.owncloud.android.lib.resources.files;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Pattern;

public class FileUtilsTest {

    @Test
    public void md5Sum() throws IOException, NoSuchAlgorithmException {
        Pattern pattern = Pattern.compile("^[0-9a-f]*$", Pattern.CASE_INSENSITIVE);
        File file;

        for (int i = 0; i < 10; i++) {
            file = new File("test");
            FileWriter writer = new FileWriter(file);

            for (int j = 0; j < new Random().nextInt(100); j++) {
                writer.write("123123123123123123123123123\n");
            }
            writer.flush();
            writer.close();

            String md5sum = FileUtils.md5Sum(file);

            System.out.println(md5sum);

            Assert.assertEquals(32, md5sum.length());
            Assert.assertTrue(md5sum, pattern.matcher(md5sum).matches());
        }
    }
}
