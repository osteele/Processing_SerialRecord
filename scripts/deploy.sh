#!/usr/bin/env bash

set -e

S3_BUCKET=assets.osteele.com
S3_DIR=processing

PATH="/opt/homebrew/opt/grep/libexec/gnubin:$PATH"

type -P xml2 > /dev/null || { echo "xml2 not found. Please install xml2:
  e.g. macOS: brew install xml2" >&2; exit 1; }

dry_run_prefix=
for arg in "$@"
do
    if [ "$arg" == "--dry-run" ]; then
        dry_run_prefix=echo
    else echo "Unknown option: $arg" >&2; exit 1; fi
done

name=$(xml2 < pom.xml | grep -oP '^/project/artifactId=\K.+')
pom_version=$(xml2 < pom.xml | grep -oP '^/project/version=\K.+')
project_version=$(grep -oP '^prettyVersion=\K.+' library.properties)

# TODO DRY release.sh
# print an error and exit if the project version and pom version differ
if [ "$pom_version" != "$project_version" ]; then
    echo "Project version ($project_version) does not match pom version ($pom_version)" >&2
    exit 1
fi

mvn clean package javadoc:javadoc assembly:single

project_file=library.properties
zip_file=target/SerialRecord-${project_version}-processing-library.zip

project_file_key=${S3_DIR}/${name}.txt
zip_key=${S3_DIR}/${name}.zip

${dry_run_prefix} aws s3 cp "$project_file" s3://${S3_BUCKET}/${project_file_key}
${dry_run_prefix} aws s3 cp "$zip_file" s3://${S3_BUCKET}/${zip_key}

${dry_run_prefix} aws s3api put-object-acl --bucket $S3_BUCKET --key $project_file_key --acl public-read
${dry_run_prefix} aws s3api put-object-acl --bucket $S3_BUCKET --key $zip_key --acl public-read
