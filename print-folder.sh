#!/bin/bash

HTML_DIR="$1/html"
mkdir -p $HTML_DIR
for FILE in $1/*; do
  [ -f "$FILE" ] || continue
  echo "Printing to HTML: $FILE "
  HTML_FILE="$FILE.html"
  java -jar ./build/libs/opensong-to-print.jar $2 < "$FILE" > "$HTML_FILE"
  mv "$HTML_FILE" "$HTML_DIR"
done
