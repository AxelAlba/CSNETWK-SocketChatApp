package chatapp.repositories;

public class FileRepository {
    private static volatile String fileContent;

    public static synchronized void setFileContent(String content) {
        fileContent = content;
    }

    public static synchronized String getFileContent() {
        return fileContent;
    }
}
