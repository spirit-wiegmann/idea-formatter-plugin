#!/bin/bash -e

rm -rf build/distributions

./gradlew buildPlugin

unzip -o build/distributions/formatter-plugin-1.0-SNAPSHOT.zip -d build/distributions

# Make sure to pull latest image before building new ones to reuse cache
# docker pull funbiscuit/idea-formatter
docker build . -t funbiscuit/idea-formatter --progress=plain --cache-from funbiscuit/idea-formatter
