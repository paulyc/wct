#!/bin/bash -x

for file in $(find . -type f -iname '*.java'); do
	iconv -f ISO-8859-1 -t UTF-8 "$file" > /tmp/f
	mv -fv /tmp/f "$file"
done
