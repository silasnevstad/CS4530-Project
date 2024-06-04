#!/bin/bash

read -sp "Enter decryption password: " PASSWORD
echo

FILE_PATH="src/main/resources"
FILES=("keystore.jks.enc" "husksheets.cer.enc" "truststore.jks.enc")

for FILE in "${FILES[@]}"; do
  DECRYPTED_FILE="${FILE%.enc}"
  openssl enc -d -aes-256-cbc -pbkdf2 -in "$FILE_PATH/$FILE" -out "$FILE_PATH/$DECRYPTED_FILE" -k "$PASSWORD"
  if [ $? -ne 0 ]; then
    echo "Failed to decrypt $FILE_PATH/$FILE"
  fi
done

echo "Files decrypted."