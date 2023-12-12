package kr.sml.masking;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

@Service
public class MaskingService implements MaskingServiceInterface {
    
    private final Path rootUploadLocation;
    private final Path rootMaskedLocation;

    public MaskingService(MaskingProperties properties) {
        if (properties.getUploadLocation().trim().length() == 0)
            throw new MaskingException("File upload location cannot be empty.");
        if (properties.getMaskedLocation().trim().length() == 0)
            throw new MaskingException("Masked file location cannot be empty.");
        
        this.rootUploadLocation = Paths.get(properties.getUploadLocation());
        this.rootMaskedLocation = Paths.get(properties.getMaskedLocation());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootUploadLocation);
            Files.createDirectories(rootMaskedLocation);
            String[] commandUpload = new String[]{"chmod", "-R", "777", rootUploadLocation.toString()};
            String[] commandMasked = new String[]{"chmod", "-R", "777", rootMaskedLocation.toString()};
            Runtime.getRuntime().exec(commandUpload);
            Runtime.getRuntime().exec(commandMasked);
        }
        catch (IOException error) {
            throw new MaskingException("Could not initialize storage", error);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootUploadLocation.toFile());
        FileSystemUtils.deleteRecursively(rootMaskedLocation.toFile());
    }

    // 업로드 요청한 파일 저장
    @Override
    @SuppressWarnings("null")
    public String storeUpload(MultipartFile file, HttpSession session, String hash) {
        String errorMessage = "OK";
        String sessionValue = session.getAttribute("userId").toString();
        String fileExtension = file.getContentType().split("/")[1];
        String fileName = sessionValue + "." + hash + "." + fileExtension;
        Path destination = this.rootUploadLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();

        if (!destination.getParent().equals(this.rootUploadLocation.toAbsolutePath())) {
            errorMessage = "Cannot store file outside current directory.";
            return errorMessage;
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            String[] command = new String[]{"chmod", "-R", "777", destination.toString()};
            Runtime.getRuntime().exec(command);
        }
        catch (IOException exception) {
            errorMessage = "Failed to store file.";
            return errorMessage;
        }
            
        return errorMessage;
    }

    @Override
    public Path loadUpload(String filename) {
        return rootUploadLocation.resolve(filename);
    }

    @Override
    public Resource loadUploadResource(String filename) {
        try {
            Path file = loadUpload(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new MaskingException("Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new MaskingException("Could not read file: ", e);
        }
    }

    @Override
    public Path loadMasked(String filename) {
        return rootMaskedLocation.resolve(filename);
    }

    @Override
    public Resource loadMaskedResource(String filename) {
        try {
            Path file = loadMasked(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new MaskingException("Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new MaskingException("Could not read file: ", e);
        }
    }

}
