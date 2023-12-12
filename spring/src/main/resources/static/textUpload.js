var maskList;
async function submitText() {
    if ($(".textarea").val().length == 0) {
        alert("Text area cannot be empty.");
    }
    else if ($(".textarea").val().length > 10000) {
        alert("Text area cannot over 10000 bytes.");
    }
    else {
        let forms = document.getElementById('textForm');
        formData = new FormData(forms);
        const response = await fetch('requestText', {method:'POST', body: formData});
        const jsonData = await response.json();
        if (jsonData["status"] == 'OK') {
            maskList = jsonData['masks'];
            var text = $(".textarea").val();
            $(".textResult").html(text);
            var count = 0;
            jsonData['masks'].forEach(element => {
                let dom = '<span class="maskText' + count + '" onmouseover="textMouseOver(\'maskText' + count + '\')" onmouseout="textMouseOut(\'maskText' + count + '\')">***</span>';
                $(".textResult").html($(".textResult").html().replace(element, dom));
                count++;
            });
        }
    }
}
function textMouseOver(maskID) {
    let classname = "." + maskID;
    let maskCount = parseInt(maskID.replace("maskText",""));
    $(classname).text(maskList[maskCount]);
}
function textMouseOut(maskID) {
    let classname = "." + maskID;
    $(classname).text("***");
}