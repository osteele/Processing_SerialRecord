#!/usr/bin/env bash

set -e

for file in examples/*; do
  echo -n "Building $file..."
  processing-java --sketch="$(pwd)/${file}" --build
done
