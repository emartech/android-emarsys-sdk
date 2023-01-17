FROM eclipse-temurin:17
LABEL maintainer="team-sdk@emarsys.com"

WORKDIR /

SHELL ["/bin/bash", "-c"]

# To avoid "tzdata" asking for geographic area
ARG DEBIAN_FRONTEND=noninteractive

# Version of tools
ARG GRADLE_VERSION=7.5
ARG ANDROID_API_LEVEL=33
ARG ANDROID_BUILD_TOOLS_LEVEL=33.0.1

# Dependencies and needed tools
RUN apt update -qq && apt install -qq -y vim git unzip libglu1 libpulse-dev libasound2 libc6  libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxi6  libxtst6 libnss3 wget

# Download commandlinetools, install packages and accept all licenses
RUN mkdir /android
RUN mkdir /android/cmdline-tools
RUN wget -q 'https://dl.google.com/android/repository/commandlinetools-linux-9123335_latest.zip' -P /tmp
RUN unzip -q -d /android/cmdline-tools /tmp/commandlinetools-linux-9123335_latest.zip

RUN yes Y | /android/cmdline-tools/cmdline-tools/bin/sdkmanager --install "build-tools;${ANDROID_BUILD_TOOLS_LEVEL}" "platforms;android-${ANDROID_API_LEVEL}" "platform-tools"
RUN yes Y | /android/cmdline-tools/cmdline-tools/bin/sdkmanager --install "build-tools;30.0.3"
RUN yes Y | /android/cmdline-tools/cmdline-tools/bin/sdkmanager --licenses

# Environment variables to be used for build
ENV ANDROID_HOME=/android
#RUN export ANDROID_HOME=/android
ENV PATH "$PATH:$GRADLE_HOME/bin:/opt/gradlew:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/tools/bin:$ANDROID_HOME/platform-tools:${ANDROID_NDK_HOME}"
ENV LD_LIBRARY_PATH "$ANDROID_HOME/emulator/lib64:$ANDROID_HOME/emulator/lib64/qt/lib"

# Clean up
RUN rm /tmp/commandlinetools-linux-9123335_latest.zip
# docker build . --platform linux/amd64 --tag eu.gcr.io/ems-mobile-sdk/android-container:1.0.5 --tag eu.gcr.io/ems-mobile-sdk/android-container:latest
# docker image push eu.gcr.io/ems-mobile-sdk/android-container:1.0.5
# docker image push eu.gcr.io/ems-mobile-sdk/android-container:latest