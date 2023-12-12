from flask import Flask, request, jsonify
import os
import requests
import detect

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
