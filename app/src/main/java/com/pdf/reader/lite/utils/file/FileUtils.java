package com.pdf.reader.lite.utils.file;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.pdf.reader.lite.R;
import com.pdf.reader.lite.data.FileData;
import com.pdf.reader.lite.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FileUtils {
    public static final String AUTHORITY_APP = "com.pdf.reader.lite.provider";

    private static final List<String> orderList = Arrays.asList(MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.SIZE);
    public static final int SORT_BY_DATE_ASC = 0;
    public static final int SORT_BY_DATE_DESC = 1;
    public static final int SORT_BY_NAME_ASC = 2;
    public static final int SORT_BY_NAME_DESC = 3;
    public static final int SORT_BY_SIZE_ASC = 4;
    public static final int SORT_BY_SIZE_DESC = 5;

    // GET PDF DETAILS
    public static String getFormattedDate(File file) {
        Date lastModDate = new Date(file.lastModified());
        String[] formatDate = lastModDate.toString().split(" ");
        String time = formatDate[3];
        String[] formatTime = time.split(":");
        String date = formatTime[0] + ":" + formatTime[1];

        return formatDate[0] + ", " + formatDate[1] + " " + formatDate[2] + " at " + date;
    }

    @SuppressLint("DefaultLocale")
    public static String getFormattedSize(int size) {
        int kb = size / 1024;
        if (kb < 100) {
            return String.format("%.2f KB", (double) size / (1024));
        } else {
            return String.format("%.2f MB", (double) size / (1024 * 1024));
        }
    }

    public static ArrayList<FileData> getExternalFileList(Context context, int order) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.SIZE};
        String selectionMimeType;
        String[] selectionArgsPdf;
        selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        selectionArgsPdf = new String[]{mimeType};

        String orderBy;
        if (order % 2 == 0) {
            orderBy = orderList.get(order / 2)+ " ASC";
        } else {
            orderBy = orderList.get(order / 2)+ " DESC";
        }

        Cursor cursor = cr.query(uri, projection, selectionMimeType, selectionArgsPdf, orderBy);
        ArrayList<FileData> fileList = new ArrayList<>();
        if (cursor != null) {

            while (cursor.moveToNext()) {

                int columnIdIndex = cursor.getColumnIndex(projection[0]);
                int columnNameIndex = cursor.getColumnIndex(projection[1]);
                int columnDateIndex = cursor.getColumnIndex(projection[2]);
                int columnSizeIndex = cursor.getColumnIndex(projection[3]);

                long fileId = -1;
                try {
                    fileId = cursor.getLong(columnIdIndex);
                } catch (Exception e) {
                    continue;
                }

                Uri fileUri = Uri.parse(uri.toString() + "/" + fileId);

                String displayName = cursor.getString(columnNameIndex);
                if (displayName == null || displayName.length() == 0) {
                    displayName = "No name";
                }

                int dateAdded;
                try {
                    dateAdded = Integer.parseInt(cursor.getString(columnDateIndex));
                } catch (Exception e) {
                    dateAdded = -1;
                }

                int size = 0;
                try {
                    size = Integer.parseInt(cursor.getString(columnSizeIndex));
                } catch (Exception e) {
                    size = -1;
                }
                String filePath = RealPathUtil.getInstance().getRealPath(context, fileUri);

                fileList.add(new FileData(displayName, filePath, fileUri, dateAdded, size));
            }
            cursor.close();
        }
        return fileList;
    }

    public static ArrayList<FileData> getAllExternalFileList(Context context, int order) {
        DirectoryUtils directoryUtils = new DirectoryUtils(context);
        ArrayList<String> fileList = new ArrayList<>();

        fileList = directoryUtils.getAllPDFsOnDevice();

        ArrayList<FileData> resultList = new ArrayList<>();
        for (String filePath: fileList) {
            try {
                File file = new File(filePath);
                Uri uri = Uri.fromFile(file);
                int size = Integer.parseInt(String.valueOf(file.length()/1024));

                FileData fileData = new FileData(getFileName(filePath), filePath, uri, (int) (file.lastModified() / 1000), size);
                resultList.add(fileData);
            } catch (Exception e) {

            }
        }

        try {
            FileSortUtils.performSortOperation(order, resultList);
        } catch (Exception e) {

        }

        return resultList;
    }

    public static void printFile(Context context, final File file) {
        final PrintDocumentAdapter mPrintDocumentAdapter = new PrintDocumentAdapterHelper(file);

        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        String jobName = context.getString(R.string.app_name) + " Print Document";

        try {
            if (printManager != null) {
                printManager.print(jobName, mPrintDocumentAdapter, null);
            } else {
                ToastUtils.showMessageShort(context, "Can not print file now.");
            }
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not print file now.");
        }
    }

    public static void shareFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri);

        shareFileWithType(context, uris, "application/pdf");
    }

    private static void shareFileWithType(Context context, ArrayList<Uri> uris, String type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_TEXT, "Share this file");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(type);

        try {
            context.startActivity(Intent.createChooser(intent, "Select app to send message…"));
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not share file now.");
        }
    }

    public static void uploadFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Document")
                .setType("application/pdf")
                .setStream(uri)
                .getIntent();

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    public static String getFileName(String path) {
        if (path == null)
            return "File name";

        int index = path.lastIndexOf("/");
        return index < path.length() ? path.substring(index + 1) : "File name";
    }

    public static int getNumberPages(String filePath) {
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (filePath != null)
                fileDescriptor = ParcelFileDescriptor.open(new File(filePath), MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);

                int numberPage = renderer.getPageCount();
                renderer.close();
                return numberPage;
            }
        } catch (Exception ignored) {}

        return 0;
    }

    public static String getFileDirectoryPath(String path) {
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    public static void deleteFileOnExist(String path) {
        if (path == null) return;

        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean checkFileExist(String path) {
        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception ignored) {
        }

        return false;
    }

    public static int renameFile(FileData fileData, String newName) {
        try {
            File currentFile = new File(fileData.getFilePath());

            if (!fileData.getFilePath().contains(fileData.getDisplayName())) {
                return -2;
            }

            String newDir = fileData.getFilePath().replace(fileData.getDisplayName(), newName);
            File newFile = new File(newDir);

            if (newFile.exists()) {
                return -1;
            }

            if (!currentFile.exists()) {
                return -2;
            }

            if (rename(currentFile, newFile)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return -2;
        }
    }

    private static boolean rename(File from, File to) {
        try {
            return from.getParentFile() != null && from.getParentFile().exists() && from.exists() && from.renameTo(to);
        } catch (Exception e) {
            return false;
        }
    }
}
