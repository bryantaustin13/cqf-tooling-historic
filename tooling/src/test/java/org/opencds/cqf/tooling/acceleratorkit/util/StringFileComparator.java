package org.opencds.cqf.tooling.acceleratorkit.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringFileComparator extends FilesComparator {

    @Override
    public void compareFilesAndAssertIfNotEqual(File file, File cf) {
        try {
            String inputFile = StringUtils.normalizeSpace(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            String compareFile = StringUtils.normalizeSpace(FileUtils.readFileToString(cf, StandardCharsets.UTF_8));
            assertEquals(inputFile.length(), compareFile.length());
            assertTrue(inputFile.equals(compareFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}