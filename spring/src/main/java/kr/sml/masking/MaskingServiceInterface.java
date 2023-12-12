package kr.sml.masking;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

public interface MaskingServiceInterface {
    void init();
    String storeUpload(MultipartFile file, HttpSession session, String hash);
    Path loadUpload(String filename);
    Resource loadUploadResource(String filename);
    Path loadMasked(String filename);
    Resource loadMaskedResource(String filename);
    void deleteAll();
}
