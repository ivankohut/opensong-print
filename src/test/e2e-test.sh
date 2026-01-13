#!/bin/bash

$(cd ../../ && ./gradlew shadowJar)
HYMNBOOK_DIR=../../build/e2e-test/hymnbook
mkdir --parents "$HYMNBOOK_DIR"
cp song2.xml "$HYMNBOOK_DIR/song2"
cp song1.xml "$HYMNBOOK_DIR/song1"
# exercise
java -jar ../../build/libs/opensong-to-print.jar "Oranžový spevníček" "$HYMNBOOK_DIR"
# verify
cmp expected-song1.html "$HYMNBOOK_DIR/html/song1.html"
cmp expected-song2.html "$HYMNBOOK_DIR/html/song2.html"
cmp expected-index.html "$HYMNBOOK_DIR/html/index.html"
