package main.chatapp.repositories;

import java.util.List;

public class FileRepository {
    private static volatile List<byte[]> fileContent;

    public static synchronized void setCurrentFile(List<byte[]> content) {
        fileContent = content;
    }

    public static synchronized List<byte[]> getFileContent() {
        return fileContent;
    }
}
