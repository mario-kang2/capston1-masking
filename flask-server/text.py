import argparse
import json
import torch
import numpy as np

from transformers import AutoTokenizer
from transformers import AutoModelForTokenClassification

def parse_opt():
    parser = argparse.ArgumentParser()
    parser.add_argument('--txt', type=str)
    opt = parser.parse_args()
    # print_args(vars(opt))
    return opt

def main(opt):
    f = open(opt.txt, 'r')
    text = f.read()

    tokenizer = AutoTokenizer.from_pretrained("soline013/KPFBERT")
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    tokenized_input = tokenizer.encode(text, return_tensors="pt", truncation=True, padding=True)
    token_split = tokenizer.convert_ids_to_tokens(tokenized_input[0])

    model = AutoModelForTokenClassification.from_pretrained("soline013/KPFBERT")
    with torch.no_grad():
        logits = model(**inputs).logits

    predictions = torch.argmax(logits, dim=2)
    pred_token_class = [model.config.id2label[t.item()] for t in predictions[0]]
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

    for i, j in enumerate(text_split):
        for k in need_mask:
            if k in j:
                text_split[i] = "***"
    text_split = ' '.join(text_split)
    print(text_split)
        

if __name__ == '__main__':
    opt = parse_opt()
    main(opt)