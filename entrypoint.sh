#!/bin/bash
set -e

# 템플릿 설정파일을 실제 경로로 복사 후 변수 치환
cp /etc/asterisk-templates/*.conf /etc/asterisk/

for conf in /etc/asterisk/*.conf; do
  sed -i \
    -e "s|\${EXTERNAL_IP}|${EXTERNAL_IP}|g" \
    -e "s|\${LOCAL_NET}|${LOCAL_NET}|g" \
    -e "s|\${SIP_1001_PASSWORD}|${SIP_1001_PASSWORD}|g" \
    -e "s|\${SIP_1002_PASSWORD}|${SIP_1002_PASSWORD}|g" \
    -e "s|\${ARI_PASSWORD}|${ARI_PASSWORD}|g" \
    "$conf"
done

exec asterisk -f -U asterisk -G asterisk -vvvvv
