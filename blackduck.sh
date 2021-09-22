curl --silent -O https://detect.synopsys.com/detect.sh
chmod +x detect.sh
./detect.sh \
--blackduck.timeout=6000 \
--blackduck.trust.cert=true \
--detect.blackduck.signature.scanner.memory=4096 \
--detect.blackduck.signature.scanner.paths=~/work/android-emarsys-sdk/android-emarsys-sdk \
--detect.code.location.name="https://github.com/emartech/android-emarsys-sdk" \
--detect.project.name="Github:emartech / android-emarsys-sdk" \
--detect.project.version.name=android-emarsys-sdk \
--detect.report.timeout=4800 \
--detect.source.path=~/work/android-emarsys-sdk/android-emarsys-sdk  \
--detect.latest.release.version=6.7.0 \
--detect.npm.include.dev.dependencies=false \
--detect.sbt.report.search.depth=4 \
--detect.gradle.path=~/work/android-emarsys-sdk/android-emarsys-sdk/gradlew