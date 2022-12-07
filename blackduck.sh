curl --silent -O https://detect.synopsys.com/detect7.sh
chmod +x detect7.sh
./detect7.sh \
--detect.timeout=6000 \
--blackduck.trust.cert=true \
--detect.blackduck.signature.scanner.memory=4096 \
--detect.blackduck.signature.scanner.paths=~/work/android-emarsys-sdk/android-emarsys-sdk \
--detect.code.location.name="https://github.com/emartech/android-emarsys-sdk" \
--detect.project.name="Github:emartech\ /\ android-emarsys-sdk" \
--detect.project.version.name=master \
--detect.source.path=~/work/android-emarsys-sdk/android-emarsys-sdk \
--detect.gradle.path=~/work/android-emarsys-sdk/android-emarsys-sdk/gradlew
--detect.sbt.report.search.depth=4 \
