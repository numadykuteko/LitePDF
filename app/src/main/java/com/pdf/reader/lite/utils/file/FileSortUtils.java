package com.pdf.reader.lite.utils.file;

import static com.pdf.reader.lite.utils.file.FileUtils.*;

import com.pdf.reader.lite.data.FileData;

import java.util.Collections;
import java.util.List;

public class FileSortUtils {

    // Sorting order constants

    private FileSortUtils() {}

    public static void performSortOperation(int option, List<FileData> pdf) {
        switch (option) {
            case SORT_BY_DATE_ASC:
                sortFilesByDateOldestToNewest(pdf);
                break;
            case SORT_BY_DATE_DESC:
                sortFilesByDateNewestToOldest(pdf);
                break;
            case SORT_BY_NAME_ASC:
                sortByNameAlphabetical(pdf);
                break;
            case SORT_BY_NAME_DESC:
                sortByNameAlphabeticalRevert(pdf);
                break;
            case SORT_BY_SIZE_ASC:
                sortFilesBySizeIncreasingOrder(pdf);
                break;
            case SORT_BY_SIZE_DESC:
                sortFilesBySizeDecreasingOrder(pdf);
                break;
        }
    }

    // SORTING FUNCTIONS

    private static void sortByNameAlphabetical(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> file.getDisplayName().compareToIgnoreCase(file2.getDisplayName()));
    }

    private static void sortByNameAlphabeticalRevert(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> file2.getDisplayName().compareToIgnoreCase(file.getDisplayName()));
    }

    private static void sortFilesByDateOldestToNewest(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> Long.compare(file.getTimeAdded(), file2.getTimeAdded()));
    }

    private static void sortFilesByDateNewestToOldest(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> Long.compare(file2.getTimeAdded(), file.getTimeAdded()));
    }

    private static void sortFilesBySizeIncreasingOrder(List<FileData> filesList) {
        Collections.sort(filesList, (file1, file2) -> Long.compare(file1.getSize(), file2.getSize()));
    }

    private static void sortFilesBySizeDecreasingOrder(List<FileData> filesList) {
        Collections.sort(filesList, (file1, file2) -> Long.compare(file2.getSize(), file1.getSize()));
    }
}
