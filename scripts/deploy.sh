#!/usr/bin/env bash

set -e

S3_BUCKET=assets.osteele.com
S3_DIR=processing

# on macOS, requires `brew install grep xml2`
PATH="/opt/homebrew/opt/grep/libexec/gnubin:$PATH"

name=$(xml2 < pom.xml | grep -oP '^/project/artifactId=\K.+')
version=$(xml2 < pom.xml | grep -oP '^/project/version=\K.+')

mvn clean package assembly:single

project_file=library.properties
zip_file=target/SerialRecord-${version}-processing-library.zip

project_file_key=${S3_DIR}/${name}.txt
zip_key=${S3_DIR}/${name}.zip

aws s3 cp "$project_file" s3://${S3_BUCKET}/${project_file_key}
aws s3 cp "$zip_file" s3://${S3_BUCKET}/${zip_key}

aws s3api put-object-acl --bucket $S3_BUCKET --key $project_file_key --acl public-read
aws s3api put-object-acl --bucket $S3_BUCKET --key $zip_key --acl public-read
