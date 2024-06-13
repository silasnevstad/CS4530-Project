#!/bin/bash

read -sp "Enter encryption password: " PASSWORD
echo

FILE_PATH="src/main/resources"
FILES=("keystore.jks" "husksheets.cer" "truststore.jks")

for FILE in "${FILES[@]}"; do
  ENCRYPTED_FILE="${FILE}.enc"
  openssl enc -e -aes-256-cbc -pbkdf2 -in "$FILE_PATH/$FILE" -out "$FILE_PATH/$ENCRYPTED_FILE" -k "$PASSWORD"
  if [ $? -ne 0 ]; then
    echo "Failed to encrypt $FILE_PATH/$FILE"
  fi
done

echo "Files encrypted."