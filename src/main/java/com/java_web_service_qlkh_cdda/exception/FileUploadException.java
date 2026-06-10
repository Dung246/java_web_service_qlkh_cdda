package com.java_web_service_qlkh_cdda.exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}