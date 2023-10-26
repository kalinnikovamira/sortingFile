package com.test.sortingfile;

import com.test.sortingfile.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Класс-тест для проверки работоспособности кода - в input.txt можно загрузить свой массив данных и проверить итог в output.txt
 */
@SpringBootTest
class SortingFileApplicationTests {

    @Autowired
    private DictionaryService service;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testSortDictionary() throws IOException {

        Resource inputResource = resourceLoader.getResource("classpath:input.txt");
        File inputFile = inputResource.getFile();

        File outputFile = new File("output.txt");

        service.sortDirectory(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

        assertOutputFileIsSorted(outputFile);
    }

    private void assertOutputFileIsSorted(File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8))) {
            String line;
            String prevLine = null;
            while ((line = reader.readLine()) != null) {
                if (prevLine != null) {
                    assertTrue(line.compareTo(prevLine) >= 0);
                }
                prevLine = line;
            }
        }
    }

}
