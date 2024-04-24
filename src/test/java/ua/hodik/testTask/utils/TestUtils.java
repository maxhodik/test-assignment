package ua.hodik.testTask.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestUtils {
    public static String readResource(String resourceName) {
        try (InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new FileNotFoundException("Resource not found: " + resourceName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

