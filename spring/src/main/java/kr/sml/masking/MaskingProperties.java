package kr.sml.masking;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("masking")
public class MaskingProperties {

    //루트 경로
    //Production 시 바꿀 것
    private String uploadPath = "/home/tomcat/";

    private String uploadLocation = uploadPath + "upload-dir";
    private String maskedLocation = uploadPath + "masked-dir";
    public String getUploadLocation() {
        return uploadLocation;
    }
    public String getMaskedLocation() {
        return maskedLocation;
    }
    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }
    public void setMaskedLocation(String maskedLocation) {
        this.maskedLocation = maskedLocation;
    }
}
