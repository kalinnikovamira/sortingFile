package com.test.sortingfile.controller;

import com.test.sortingfile.DictionarySorter;
import com.test.sortingfile.service.DictionaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * Контроллер, обеспечивающий простой интерфейс:
 * - можем загрузить наш файл
 * - можем установить параметр chunkSize для регулирования работы приложения
 * - можем скачать отсортированный файл по ссылке
 */
@Controller
@Api(description = "Контроллер для возможности в приложении удобно добавить свой файл и получить результат")
public class DictionaryController {
    private DictionaryService dictionaryService;

    /*
    Указываем путь в property, куда будем записывать результат.
    Эту логику можно было бы реализовать так, чтобы пользователь указывал путь самостоятельно,
    но поскольку есть функционал скачивания файла, решила оставить так.
     */
    @Value("${file.upload.dir}")
    private String uploadDirectory;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("chunkSize", 100000);
    }


    @GetMapping("/")
    @ApiOperation("Основной интерфейс для взаимодействия с приложением")
    public String index() {
        return "client";
    }

    @PostMapping("/sortDictionary")
    @ApiOperation("Получение результата на основе данных пользователя")
    public ResponseEntity<String> sortDictionary(@RequestParam("inputFile") MultipartFile inputFile,
                                                 @RequestParam("chunkSize") int chunkSize) {

        if (inputFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Пожалуйста, выберите файл для загрузки.");
        }

        try {
            File tempFile = File.createTempFile("temp", ".txt");

            inputFile.transferTo(tempFile);


            String outputFilePath = uploadDirectory + "/output.txt";

            dictionaryService.setChunkSize(chunkSize);
            dictionaryService.sortDirectory(tempFile.getAbsolutePath(), outputFilePath);

            return ResponseEntity.ok("Файл успешно отсортирован.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    @ApiOperation("Получение отсортированного файла в виде ссылки на скачивание")
    public ResponseEntity<Resource> downloadSortedFile(HttpServletResponse response) {
        File outputFile = new File(uploadDirectory + "/output.txt");
        if (outputFile.exists()) {

            // Определяем параметры для файла на скачивание
            response.setHeader("Content-Disposition", "attachment; filename=sorted_dictionary.txt");

            FileSystemResource resource = new FileSystemResource(outputFile);

            return ResponseEntity.ok()
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}