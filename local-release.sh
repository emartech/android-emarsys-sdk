#!/usr/bin/env bash
export EXCLUDE_GOOGLE_SERVICES_API_KEY=true

./gradlew clean lint assembleRelease publishToSonatype