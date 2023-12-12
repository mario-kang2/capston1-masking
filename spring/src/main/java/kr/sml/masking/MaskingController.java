package kr.sml.masking;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MaskingController {
    private final MaskingServiceInterface maskingService;

    public MaskingController(MaskingServiceInterface maskingService) {
        this.maskingService = maskingService;
    }

    // 텍스트 업로드 폼 표시
    @GetMapping("/textMask")
    public String uploadTextForm(Model model) throws IOException {
        return "textMask";
    }

    // 이미지 업로드 폼 표시
    @GetMapping("/imageMask")
    public String uploadImageForm(Model model) throws IOException {
        return "imageMask";
    }

    // 비디오 업로드 폼 표시
    @GetMapping("/videoMask")
    public String uploadVideoForm(Model model) throws IOException {
        return "videoMask";
    }

    // 텍스트 업로드
    @PostMapping("/requestText")
    public @ResponseBody textJson handleTextUpload(HttpServletRequest request) throws Exception {
        String text = request.getParameter("textarea");
        textJson jsonBody = new textJson();
        if (text.isEmpty()) {
            jsonBody.setStatus("Error");
            jsonBody.setError("Text is empty.");
            return jsonBody;
        }
        else if (text.length() > 10000) {
            jsonBody.setStatus("OK");
            jsonBody.setError("Text length overs 10000 letters.");
            return jsonBody;
        }
        else {
            // Flask 연동 부분. 임시로 작성
            List<String> masks = new ArrayList<String>();
            Matcher matcher = Pattern.compile("[0-9]{6}-[0-9]{7}").matcher(text);
            while (matcher.find())
                masks.add(matcher.group());
            jsonBody.setStatus("OK");
            jsonBody.setMasks(masks);
            return jsonBody;
        }
    }

    // 파일 업로드
    // 세션이 활성화되어야 파일을 내려받을 수 있게 하여 다른 이용자의 무단 접근 차단
    @PostMapping(value = {"/imageUpload", "/videoUpload"})
    @SuppressWarnings("null")
    public @ResponseBody fileUploadJson handleFileUpload(@RequestParam("imagefile") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        fileUploadJson json = new fileUploadJson();
        if (file.isEmpty()) { // 빈 항목을 보냄
            json.setStatus("Error");
            json.setError("Empty file cannot be uploaded.");
            return json;
        }
        if (!file.getContentType().startsWith("image/") && !file.getContentType().startsWith("video/")) { // 이미지나 동영상이 아닌 파일을 보냄
            json.setStatus("Error");
            json.setError("Not an image or a video file cannot be uploaded.");
            return json;
        }
        if (file.getContentType().startsWith("image/") && file.getSize() > 10485760) { // 10MB가 넘는 이미지 파일을 보냄
            json.setStatus("Error");
            json.setError("An image over 10MB cannot be uploaded.");
            return json;
        }
        if (file.getContentType().startsWith("video/") && file.getSize() > 524288000) { // 500MB가 넘는 동영상 파일을 보냄
            json.setStatus("Error");
            json.setError("A video over 500MB cannot be uploaded.");
            return json;
        }
        
        // 업로드한 파일의 SHA-256 Hash 생성
        String checksum = new String();
        try {
            byte[] fileData = file.getBytes();
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(fileData);
            checksum = new BigInteger(1, hash).toString(32);
        }
        catch (Exception exception) {

        }

        // 세션 확인 및 없을 시 생성
        HttpSession session = httpServletRequest.getSession(true);
        if (session.getAttribute("userId") == null || session.getAttribute("userId").toString().isEmpty()) {
            String sessionId = UUID.randomUUID().toString();
            session.setAttribute("userId", sessionId);
            session.setMaxInactiveInterval(3600);
        }

        // 파일 저장
        String errorMessage = maskingService.storeUpload(file, session, checksum);
        String fileExtension = file.getContentType().split("/")[1];
        if (errorMessage == "OK") {
            json.setStatus("OK");
            json.setFileName(checksum + "." + fileExtension);
            json.setFileType(file.getContentType());
        }
        else {
            json.setStatus("Error");
            json.setError(errorMessage);
        }

        return json;
    }

    // 파일 표시
    // 세션이 활성화되어야 파일을 내려받을 수 있게 하여 다른 이용자의 무단 접근 차단
    @GetMapping(value = {"/imageMask/files/{filename:.+}", "/videoMask/files/{filename:.+}"})
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            throw new MaskingException("Session is expired");
        }
        String sessionValue = session.getAttribute("userId").toString();
        String newFileName = sessionValue + "." + filename;
        Resource file = maskingService.loadUploadResource(newFileName);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(file);
    }

    //마스킹된 이미지 전달
    //TODO: 블러 옵션 넣어주기
    @GetMapping("/imageMask/mask/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveMaskedImage(@PathVariable String filename, HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            throw new MaskingException("Session is expired");
        }
        String sessionValue = session.getAttribute("userId").toString();
        String newFileName = sessionValue + "." + filename;

        RestTemplate rest = new RestTemplate();
        String urlString = "http://localhost:48080/detectImage";
        String filePath = "/home/tomcat/upload-dir/" + newFileName;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> request = new HttpEntity<>(headers);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(urlString).queryParam("path", filePath);
        ResponseEntity<String> responseEntity = rest.exchange(uriBuilder.toUriString(), HttpMethod.GET, request, String.class);
        if (responseEntity.getStatusCode() == HttpStatusCode.valueOf(200)) {
            Resource file = maskingService.loadMaskedResource(newFileName);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(file);
        }
        else {
            return ResponseEntity.badRequest().body(null);
        }   
    }
}

// 텍스트 업로드 요청 JSON 반환 형식
class textJson {
    private String status; // 결과
    private List<String> masks; // 마스킹 결과. 오류 시 비워둠
    private String error; // 오류 설명. 정상 시 비워둠

    public String getStatus() {return status;}
    public List<String> getMasks() {return masks;}
    public String getError() {return error;}

    public void setStatus(String status) {this.status = status;}
    public void setMasks(List<String> masks) {this.masks = masks;}
    public void setError(String error) {this.error = error;}
}

// 파일 업로드 요청 JSON 반환 형식
class fileUploadJson {
    private String status; // 결과
    private String fileName; // 파일명. 오류 시 비워둠
    private String fileType; // 파일 종류. 오류 시 비워둠
    private String error; // 오류 설명. 정상 시 비워둠

    public String getStatus() {return status;}
    public String getFileName() {return fileName;}
    public String getFileType() {return fileType;}
    public String getError() {return error;}

    public void setStatus(String status) {this.status = status;}
    public void setFileName(String fileName) {this.fileName = fileName;}
    public void setFileType(String fileType) {this.fileType = fileType;}
    public void setError(String error) {this.error = error;}
}