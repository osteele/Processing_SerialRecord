#!/usr/bin/env bash

set -e

type -P ghr > /dev/null || { echo "ghr not found. Please install ghr:
  https://github.com/tcnksm/ghr#install" >&2; exit 1; }

# on macOS, requires `brew install grep`
PATH="/opt/homebrew/opt/grep/libexec/gnubin:$PATH"

OWNER=osteele
REPO=PROCESSING_SerialRecord
project_file=library.properties

skip_tag=false
force_tag_option=

# loop through all the command line arguments. set skip-tag and force_tag
# based on the arguments.
for arg in "$@"
do
    if [ "$arg" == "--skip-tag" ]; then
        skip_tag=true
    elif [ "$arg" == "--force-tag" ]; then
        force_tag_option="--force"
    fi
done

name=$(grep -oP '^name=\K.+' $project_file)
version=$(grep -oP '^prettyVersion=\K.+' $project_file)
tag="v${version}"

zip_file=target/SerialRecord-${version}-processing-library.zip
mvn clean package assembly:single

mkdir -p dist
find dist -type f -delete
cp $project_file dist/
mv ${zip_file} dist/${name}.zip

# skip the tag if skip_tag is set
if [ "$skip_tag" = false ]; then
  git tag ${force_tag_option} -a "${tag}" -m "Release $version"
  git push ${force_tag_option} origin "${tag}"
fi


ghr -u "${OWNER}" -r "${REPO}" -prerelease "${tag}" dist
open "https://github.com/${OWNER}/${REPO}/releases"
