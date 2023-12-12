import torch
import json
import cv2
import os

def detect_and_save(path):
    # Model
    model = torch.hub.load('ultralytics/yolov5', 'custom', 'best.pt')

    results = model(path)

    re = results.pandas().xyxy
    jsonData = json.loads(re[0].to_json(orient="records"))

    imageData = cv2.imread(path)

    for data in jsonData:
        xmin = int(data['xmin'])
        ymin = int(data['ymin'])
        xmax = int(data['xmax'])
        ymax = int(data['ymax'])
        blur = imageData[ymin:ymax, xmin:xmax]
        blur = cv2.blur(blur, (50,50))
        imageData[ymin:ymax, xmin:xmax] = blur

    maskSavePath = r'/home/tomcat/masked-dir/'
    fileName = path.split('/')[-1]

    cv2.imwrite(os.path.join(maskSavePath, fileName), imageData)
