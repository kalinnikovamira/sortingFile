package com.test.sortingfile.service;

import com.test.sortingfile.DictionarySorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DictionaryService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DictionarySorter dictionarySorter;
    public DictionaryService(DictionarySorter dictionarySorter) {
        this.dictionarySorter = dictionarySorter;
    }


    public void sortDirectory(String absolutePath, String outputFilePath) throws IOException {
        dictionarySorter.sortDictionary(absolutePath, outputFilePath);

        log.info("the file was sorted");
    }

    public void setChunkSize(int chunkSize) {
        dictionarySorter.setChunkSize(chunkSize);

        log.info("the chunkSize was updated: {}", chunkSize);
    }
}
