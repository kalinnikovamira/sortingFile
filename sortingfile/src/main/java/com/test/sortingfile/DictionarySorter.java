package com.test.sortingfile;

import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Класс DictionarySorter представляет собой класс, где содержится основная логика сортировки.
 * Используется внешняя сортировка слиянием:
 *
 * - файл разбивается на несколько фрагментов - размер можно указать пользователям
 * в зависимости от размера их исходного файла.
 * - каждый из фрагментов сортируется
 * - затем фрагменты сливаются в один, производя сортировку
 *
 *
 * P.S В идеале бы сюда прикрутить многопоточку, так как метод будет выполняться очень долго при огромном объеме,
 * но у меня уже не хватило времени покопаться :(
 */
@Component
public class DictionarySorter {

    // Размер каждого фрагмента файла (максимально допустимое количество строк, стоило бы реализовать в виде макс исп памяти)
    private int chunkSize = 100000;

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }


    // Метод, использующийся в сервисе, соединяющий в себе всю логику алгоритма, чтобы не писать один огромный метод
    public void sortDictionary(String inputFilePath, String outputFilePath) throws IOException {

        splitAndSortChunks(new File(inputFilePath));

        mergeSortedChunks(new File(outputFilePath));
    }

    // Метод, что распределяет фрагменты на временные файлы
    private void splitAndSortChunks(File inputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), UTF_8))) {
            String line;

            // Переменная для отслеживания текущего индекса для фрагмента
            int chunkIndex = 0;

            // Итерируемся по всему фрагменту и записываем во временный файл, который будем сортировать
            while ((line = reader.readLine()) != null) {
                File chunkFile = new File("chunk_" + chunkIndex + ".txt");

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(chunkFile))) {
                    int linesCount = 0;
                    while (line != null && linesCount < chunkSize) {
                        writer.write(line);
                        writer.newLine();
                        linesCount++;
                        line = reader.readLine();
                    }
                }

                sortChunk(chunkFile);

                chunkIndex++;
            }
        }
    }

    // Логика сортировки фрагментов
    private void sortChunk(File chunkFile) throws IOException {

        SortedSet<String> lines = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(chunkFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chunkFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // Вспомогательная структура для сравнения строк между собой
    private record LineWrapper(String line, BufferedReader reader) implements Comparable<LineWrapper> {
        @Override
            public int compareTo(LineWrapper other) {
                return line.compareTo(other.line);
            }
    }


    private void mergeSortedChunks(File outputFile) throws IOException {
        PriorityQueue<LineWrapper> heap = new PriorityQueue<>();

        // Создаем BufferedReader для чанков и кладем в PriorityQueue
        for (int i = 0; ; i++) {
            File chunkFile = new File("chunk_" + i + ".txt");
            if (!chunkFile.exists()) {
                break;
            }

            BufferedReader reader = new BufferedReader(new FileReader(chunkFile));
            String line = reader.readLine();
            if (line != null) {
                heap.add(new LineWrapper(line, reader));
            }
        }

        // Создаем итоговый файл, сортировка происходит за счет использования priority queue
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            while (!heap.isEmpty()) {
                LineWrapper lineWrapper = heap.poll();
                writer.write(lineWrapper.line);
                writer.newLine();

                String nextLine = lineWrapper.reader.readLine();
                if (nextLine != null) {
                    heap.add(new LineWrapper(nextLine, lineWrapper.reader));
                } else {
                    lineWrapper.reader.close();
                }
            }
        }

        // Удаление мусора (использующихся временных файлов)
        for (int i = 0; ; i++) {
            File chunkFile = new File("chunk_" + i + ".txt");
            if (!chunkFile.exists()) {
                break;
            }
            chunkFile.delete();
        }
    }

}
