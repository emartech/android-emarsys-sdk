.PHONY: base64-secret-to-file build-test check-env help create-sample-release-bundle create-testing-apks lint prepare-ci run-github-workflow-locally test-android-firebase test-android-firebase-emulator release-to-sonatype prepare-sample-release
.DEFAULT_GOAL := help
SHELL := /bin/bash

ifneq (,$(wildcard .env))
include .env
export
endif

REQUIRED_VARS := $(shell cat .env.example | sed 's/=.*//' | xargs)
check-env:
	@MISSING_VARS=""; \
	for var in $(REQUIRED_VARS); do \
		if [ -z "$${!var+x}" ]; then \
			MISSING_VARS="$$MISSING_VARS $$var"; \
		fi; \
	done; \
	if [ -n "$$MISSING_VARS" ]; then \
		echo "Missing environment variables:$$MISSING_VARS"; \
		echo "Please set them in your .env file or as system environment variables. Check https://secret.emarsys.net/cred/detail/18243/"; \
		exit 1; \
	fi

help: check-env ## Show this help
	@echo "Targets:"
	@fgrep -h "##" $(MAKEFILE_LIST) | grep ":" | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/\(.*\):.*##[ \t]*/    \1 ## /' | sort | column -t -s '##'
	@echo

base64-secret-to-file: check-env ## decode base64 secret to path
	@./gradlew base64EnvToFile -PpropertyName=$(SECRET) -Pfile=$(FILE)

build-test: check-env ## builds android tests excluding and lint
	@./gradlew clean assembleAndroidTest -x lint

create-testing-apks: check-env ## create apks for testing
	@./gradlew assembleAndroidTest -x :sample:test

create-sample-release-bundle: check-env ## create sample app release bundle
	@./gradlew :sample:bundleRelease

create-sample-release-apk: check-env ## create sample app release bundle
	@./gradlew :sample:assembleRelease

lint: check-env ## run lint
	@./gradlew lint

prepare-ci: check-env ## setup prerequisites for pipeline
	@echo $ANDROID_HOME > local.properties
	@./gradlew base64EnvToFile -PpropertyName=GOOGLE_SERVICES_JSON_BASE64 -Pfile=./sample/google-services.json

prepare-release: check-env ## setup prerequisites for release
	@./gradlew base64EnvToFile -PpropertyName=SONATYPE_SIGNING_SECRET_KEY_RING_FILE_BASE64 -Pfile=./secring.asc.gpg

prepare-sample-release: check-env ## prepares .jks file for sample release
	@./gradlew base64EnvToFile -PpropertyName=ANDROID_RELEASE_STORE_FILE_BASE64 -Pfile=sample/mobile-team-android.jks \
	&& ./gradlew base64EnvToFile -PpropertyName=GOOGLE_PLAY_STORE_SEVICE_ACCOUNT_JSON_BASE64 -Pfile=sample/google-play-store-service-account.json

test-android-firebase-emulator: check-env ## run Android Instrumented tests on emulators on Firebase Test Lab
	@gcloud firebase test android run \
       --type instrumentation \
       --app ./sample/build/outputs/apk/androidTest/debug/sample-debug-androidTest.apk \
       --test ./$(MODULE_NAME)/build/outputs/apk/androidTest/debug/$(MODULE_NAME)-debug-androidTest.apk \
       --device model=Pixel2.arm,version=28,locale=en,orientation=portrait  \
       --device model=Pixel2.arm,version=30,locale=en,orientation=portrait \
       --device model=SmallPhone.arm,version=35,locale=en,orientation=portrait \
       --client-details matrixLabel="Android Emarsys SDK - virtual devices"

test-android-firebase: check-env ## run Android Instrumented tests on real devices on Firebase Test Lab
	@gcloud firebase test android run \
       --type instrumentation \
       --app ./sample/build/outputs/apk/androidTest/debug/sample-debug-androidTest.apk \
       --test ./$(MODULE_NAME)/build/outputs/apk/androidTest/debug/$(MODULE_NAME)-debug-androidTest.apk \
       --device model=Pixel2.arm,version=29,locale=en,orientation=portrait  \
       --device model=redfin,version=30,locale=en,orientation=portrait  \
       --device model=caymanlm,version=31,locale=en,orientation=portrait \
       --device model=bluejay,version=32,locale=en,orientation=portrait \
       --device model=felix,version=33,locale=en,orientation=portrait \
       --client-details matrixLabel="Android Emarsys SDK - physical devices"

run-github-workflow-locally: check-env ## needs act to be installed: `brew install act` and docker running. Pass in workflow path to run
	@act --secret-file ./workflow.secrets  -W $(WORKFLOW_PATH) --container-architecture linux/amd64

release: check-env prepare-release prepare-sample-release ## release to sonatype
	@./gradlew assembleRelease && ./gradlew publishToSonatype
release-locally: check-env prepare-release prepare-sample-release ## release to mavenLocal
	@./gradlew assembleRelease && ./gradlew publishToMavenLocal