#!/bin/sh

input_file=$1

curl -XPOST -H 'x-calais-licenseID: jvuuy7tgzgggduax5yqmw7jg' \
	-H 'Content-Type: text/html' -H 'Accept: xml/rdf' \
	-H 'enableMetadataType: GenericRelations' \
	http://api.opencalais.com/tag/rs/enrich \
	-d@$input_file \
	| xmllint --format -
