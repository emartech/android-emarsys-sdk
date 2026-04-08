FROM eclipse-temurin:17
LABEL maintainer="team-sdk@emarsys.com"

WORKDIR /

SHELL ["/bin/bash", "-c"]

# To avoid "tzdata" asking for geographic area
ARG DEBIAN_FRONTEND=noninteractive

# Version of tools
ARG GRADLE_VERSION=8.13
ARG ANDROID_API_LEVEL=36
ARG ANDROID_BUILD_TOOLS_LEVEL=36.0.0

# Dependencies and needed tools
RUN apt update -qq && apt install -qq -y vim git unzip libglu1 libpulse-dev libasound2t64 libc6 libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxi6 libxtst6 libnss3 wget curl

# Install Gradle
RUN mkdir -p /opt/gradle && \
    wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt/gradle && \
    rm /tmp/gradle.zip && \
    mv /opt/gradle/gradle-${GRADLE_VERSION} /opt/gradle/gradle

# Download commandlinetools, install packages and accept all licenses
RUN mkdir -p /android/cmdline-tools
RUN wget -q 'https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip' -P /tmp
RUN unzip -q -d /android/cmdline-tools /tmp/commandlinetools-linux-11076708_latest.zip && \
    mv /android/cmdline-tools/cmdline-tools /android/cmdline-tools/latest

RUN yes Y | /android/cmdline-tools/latest/bin/sdkmanager --sdk_root=/android --install "build-tools;${ANDROID_BUILD_TOOLS_LEVEL}" "platforms;android-${ANDROID_API_LEVEL}" "platform-tools"
RUN yes Y | /android/cmdline-tools/latest/bin/sdkmanager --sdk_root=/android --install "build-tools;35.0.0"
RUN yes Y | /android/cmdline-tools/latest/bin/sdkmanager --sdk_root=/android --licenses

# Environment variables to be used for build
ENV ANDROID_HOME=/android
ENV GRADLE_HOME=/opt/gradle/gradle
ENV PATH=${PATH}:${GRADLE_HOME}/bin:/opt/gradlew:${ANDROID_HOME}/emulator:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools
ENV LD_LIBRARY_PATH=${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/qt/lib

# Clean up
RUN rm /tmp/commandlinetools-linux-11076708_latest.zip
# docker build . --platform linux/amd64 --tag eu.gcr.io/ems-mobile-sdk/android-container:1.0.5 --tag eu.gcr.io/ems-mobile-sdk/android-container:latest
# docker image push eu.gcr.io/ems-mobile-sdk/android-container:1.0.5
# docker image push eu.gcr.io/ems-mobile-sdk/android-container:latest