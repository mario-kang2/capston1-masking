import argparse
import json
import torch
import cv2
import numpy as np
import subprocess

from transformers import AutoTokenizer
from transformers import AutoModelForTokenClassification

def parse_opt():
    parser = argparse.ArgumentParser()
    parser.add_argument('--img', type=str)
    parser.add_argument('--txt', type=str)
    opt = parser.parse_args()
    # print_args(vars(opt))
    return opt

def main(opt):
    result = subprocess.run(['python', 'tools/infer/predict_system.py', "--image_dir="+opt.img, '--det_model_dir=./Multilingual_PP-OCRv3_det_infer', '--rec_model_dir=korean_PP-OCRv3_rec_infer', '--rec_char_dict_path=ppocr/utils/dict/korean_dict.txt', '--vis_font_path=doc/fonts/korean.ttf'])
    
    f = open(opt.txt, 'r')
    txt = f.readlines()
    txt[0] = txt[0].replace("\n", "")
    txt[0] = txt[0].replace("\t", "")
    dict = json.loads(txt[0])
    
    text = []
    for i, j in enumerate(dict):
        text.append(dict[i]['transcription'])
    text = ' '.join(text)

    tokenizer = AutoTokenizer.from_pretrained("soline013/KPFBERT")
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    tokenized_input = tokenizer.encode(text, return_tensors="pt", truncation=True, padding=True)
    token_split = tokenizer.convert_ids_to_tokens(tokenized_input[0])

    model = AutoModelForTokenClassification.from_pretrained("soline013/KPFBERT")
    with torch.no_grad():
        logits = model(**inputs).logits

    predictions = torch.argmax(logits, dim=2)
    pred_token_class = [model.config.id2label[t.item()] for t in predictions[0]]
    pred_token_class
    for i in range(len(pred_token_class)):
        print(pred_token_class[i], token_split[i])

    tmp = []
    label = ['LABEL_96', 'LABEL_246', 'LABEL_91', 'LABEL_241', 'LABEL_133', 'LABEL_70', 'LABEL_193', 'LABEL_265', 'LABEL_82', 'LABEL_232', 'LABEL_69']
    for i in range(len(pred_token_class)):
        if pred_token_class[i] in label:
            tmp.append(token_split[i])
    tmp = tokenizer.convert_tokens_to_string(tmp).split()

    text_split = text.split()
    need_mask = []
    for i in text_split:
        for j in range(len(tmp)):
            if tmp[j] in i:
                need_mask.append(i)

    need_mask = list(set(need_mask))

    x1, x2, x3, x4 = [], [], [], []
    img = cv2.imread(opt.img)

    for i, j in enumerate(dict):
        for k in need_mask:
            if k == j['transcription']:
                x1, x2, x3, x4 = j['points'][0], j['points'][1], j['points'][2], j['points'][3]
                pts = np.array([x1, x2, x3, x4], np.int32)
                pts_re = pts.reshape((-1, 1, 2))
                
                blurred_image = cv2.GaussianBlur(img, (43, 43), 80)
                mask = np.zeros(img.shape, dtype=np.uint8)
                channel_count = img.shape[2]
                ignore_mask_color = (255,)*channel_count
                cv2.fillPoly(mask, [pts_re], ignore_mask_color)
                mask_inverse = np.ones(mask.shape).astype(np.uint8)*255 - mask
                img = cv2.bitwise_and(blurred_image, mask) + cv2.bitwise_and(img, mask_inverse)

    cv2.imshow("img", img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

if __name__ == '__main__':
    opt = parse_opt()
    main(opt)