// 업로드 이미지 파일 크기 제한
$(function() {
    $("input[name=imagefile").on("change", function() {
        let maxSize = 10485760; // 10MB
        let fileSize = this.files[0].size;
        if (fileSize > maxSize) {
            alert("An image over 10MB cannot be uploaded.");
            $(this).val('');
            return;
        }
    });
    $("input[name=videofile").on("change", function() {
        let maxSize = 524288000; // 500MB
        let fileSize = this.files[0].size;
        if (fileSize > maxSize) {
            alert("A video over 500MB cannot be uploaded.");
            $(this).val('');
            return;
        }
    });
});

// 이미지 업로드 및 분석
async function submitImage() {
    if ($(".imageMaskFileArea").val() == "") {
        alert("An image could not be empty.");
    }
    else {
        let forms = document.getElementById('imageForm');
        formData = new FormData(forms);
        $.ajax({
            url: 'imageUpload',
            type: 'POST',
            data: formData,
            contentType: false,
            processData: false,
            xhr: function() {
                var xhr = new window.XMLHttpRequest();
                xhr.upload.addEventListener("progress", function(event) {
                    if (event.lengthComputable) {
                        var percent = event.loaded / event.total;
                        percent = parseInt(percent * 100);
                        $("#uploadProgress").attr('aria-valuenow', percent);
                        $("#uploadProgressbar").css('width', percent+'%');
                    }
                }, false);
                return xhr;
            },
            beforeSend: function() {
                $("#uploadProgress").show();
            },
            complete: function() {
                $("#uploadProgress").hide();
            },
            success:function(response) {
                if (response["status"] == 'OK') {
                    let fileName = response['fileName'];
                    $(".beforeMaskImage").attr("src", "imageMask/files/"+fileName);
                    $(".afterMaskImage").attr("src", "imageMask/mask/"+fileName);
                    $(".maskImageDownloadLink").attr("href", "imageMask/mask/"+fileName);
                    $(".maskImageDownloadLink").text("Download "+fileName);
                }
            }
        });
    }
}

// 동영상 업로드 및 분석
async function submitVideo() {
    if ($(".videoMaskFileArea").val() == "") {
        alert("A video could not be empty.");
    }
    else {
        let forms = document.getElementById('videoForm');
        formData = new FormData(forms);
        $.ajax({
            url: 'videoUpload',
            type: 'POST',
            data: formData,
            contentType: false,
            processData: false,
            xhr: function() {
                var xhr = new window.XMLHttpRequest();
                xhr.upload.addEventListener("progress", function(event) {
                    if (event.lengthComputable) {
                        var percent = event.loaded / event.total;
                        percent = parseInt(percent * 100);
                        $("#uploadProgress").attr('aria-valuenow', percent);
                        $("#uploadProgressbar").css('width', percent+'%');
                    }
                }, false);
                return xhr;
            },
            beforeSend: function() {
                $("#uploadProgress").show();
            },
            complete: function() {
                $("#uploadProgress").hide();
            },
            success:function(response) {
                if (response["status"] == 'OK') {
                    let fileName = response['fileName'];
                    $(".beforeMaskVideo").attr("src", "videoMask/files/"+fileName);
                    $(".beforeMaskVideo").attr("type", response['fileType']);
                    $(".beforeMaskVideo").text("Your browser does not support this type of video.");
                    $(".afterMaskVideo").attr("src", "videoMask/mask/"+fileName);
                    $(".afterMaskVideo").attr("type", response['fileType']);
                    $(".afterMaskVideo").text("Your browser does not support this type of video.");
                    $(".maskVideoDownloadLink").attr("href", "videoMask/mask/"+fileName);
                    $(".maskVideoDownloadLink").text("Download "+fileName);
                }
            }
        });
    }
}