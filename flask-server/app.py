from flask import Flask, request, jsonify
import os
import requests
import detect
import text
import PaddleOCR.ocr as ocr

app = Flask(__name__)
@app.route('/')
def index():
    return 'Hello!'

@app.route('/detectImage', methods=['GET'])
def handle_image_detect():
    imagePath = request.args.get('path')
    # 이미지 저장 디렉토리
    upload_dir = '/home/tomcat/masked_dir'
    detect.detect_and_save(imagePath)
    return jsonify({'message': 'OK'}), 200

# @app.route('/', methods=['GET'])
def handle_ocr():
    ocrPath = request.args.get('path') # path need to include img path and txt path
    # upload_dir = '/home/tomcat/masked_dir'
    ocr.main(ocrPath) # ocrPath = opt.img, opt.txt
    return jsonify({'message': 'OK'}), 200

# @app.route('/', methods=['GET'])
def handle_text_detect():
    textPath = request.args.get('path') # path need to include txt path
    # upload_dir = '/home/tomcat/masked_dir'
    text.main(textPath) # textPath = opt.txt
    return jsonify({'message': 'OK'}), 200