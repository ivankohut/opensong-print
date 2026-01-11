#!/bin/bash

$(cd ../../ && ./gradlew shadowJar)
java -jar ../../build/libs/opensong-to-print.jar "Oranžový spevníček" < song.xml > output.html
cmp expected.html output.html
