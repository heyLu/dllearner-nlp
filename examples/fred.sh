#!/bin/sh

input_file=$1

curl -X GET -G \
	-H 'Accept: text/turtle' \
	--data-urlencode text=@$1 \
	http://wit.istc.cnr.it/stlab-tools/fred
