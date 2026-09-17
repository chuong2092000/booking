package project.hotelservice.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ByteArrayMultipartFile implements MultipartFile {

    private final byte[] content;
    private final String name;
    private final String originalFilename;


    public ByteArrayMultipartFile(byte[] content, String name, String originalFilename) {
        this.content = content;
        this.name = name;
        this.originalFilename = originalFilename;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        if (originalFilename == null) {
            return "application/octet-stream";
        }

        String lowerCaseName = originalFilename.toLowerCase();

        if (lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerCaseName.endsWith(".png")) {
            return "image/png";
        } else if (lowerCaseName.endsWith(".webp")) {
            return "image/webp";
        }

        return "application/octet-stream";
    }

    @Override
    public boolean isEmpty() {
        return this.content == null || this.content.length == 0;
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(this.content);
        }
    }
}
