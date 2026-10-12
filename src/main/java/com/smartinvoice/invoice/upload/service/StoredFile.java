package com.smartinvoice.invoice.upload.service;

public class StoredFile {

    private String fileId;
    private String filePath;
    private String originalFilename;

    public StoredFile(String fileId, String filePath, String originalFilename) {
        this.fileId = fileId;
        this.filePath = filePath;
        this.originalFilename = originalFilename;
    }

    public String getFileId() { return fileId; }

    public String getFilePath() { return filePath; }

    public String getOriginalFilename() { return originalFilename; }
}
