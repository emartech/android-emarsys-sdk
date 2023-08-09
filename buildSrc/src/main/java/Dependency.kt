object Versions {
    const val kotlin_version = "1.9.0"
    const val kotlin_coroutines_version = "1.7.0"
    const val kotlin_test_version = "3.4.2"
    const val mockito_version = "4.9.0"
    const val mockito_core_version = "4.9.0"
    const val firebase_messaging_version = "23.2.1"
    const val support_test_version = "1.5.2"
    const val support_rules_test_version = "1.5.0"
    const val support_library = "1.6.1"
    const val support_test_extensions = "1.1.5"
    const val support_test_fragment = "1.5.6"
    const val support_annotations = "1.6.0"
    const val buildToolsVersion = "34.0.0"
    const val multiDexVersion = "2.0.0"
    const val kotlinMockito = "4.1.0"
    const val location_services_version = "21.0.1"
    const val espresso_idling_resources = "3.5.1"
    const val archLifecycleVersion = "2.6.1"
    const val java8LifecycleVersion = "2.6.1"
    const val securityCryptoVersion = "1.1.0-alpha06"
    const val byte_buddy_version = "1.14.4"
    const val google_tink_version = "1.9.0"
    const val huawei_agconnect_core_version = "1.9.1.300"
    const val huawei_hms_push_version = "6.11.0.300"
    const val mockkVersion = "1.13.5"
    const val webkit = "1.7.0"
}

object Libs {
    const val support_annotations = "androidx.annotation:annotation:${Versions.support_annotations}"
    const val support_appcompat = "androidx.appcompat:appcompat:${Versions.support_library}"
    const val firebase_messaging =
        "com.google.firebase:firebase-messaging:${Versions.firebase_messaging_version}"
    const val espresso_idling_resources =
        "androidx.test.espresso:espresso-idling-resource:${Versions.espresso_idling_resources}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_version}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_version}"
    const val kotlin_stdlib =
        "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_version}"
    const val kotlin_coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin_coroutines_version}"
    const val location_services =
        "com.google.android.gms:play-services-location:${Versions.location_services_version}"
    const val lifecycle_process =
        "androidx.lifecycle:lifecycle-process:${Versions.archLifecycleVersion}"
    const val lifecycle_extension_java_8 =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.java8LifecycleVersion}"
    const val security_crypto =
        "androidx.security:security-crypto:${Versions.securityCryptoVersion}"
    const val google_tink = "com.google.crypto.tink:tink-android:${Versions.google_tink_version}"
    const val webkit = "androidx.webkit:webkit:${Versions.webkit}"
    const val huawei_agconnect_core =
        "com.huawei.agconnect:agconnect-core:${Versions.huawei_agconnect_core_version}"
    const val huawei_hms_push = "com.huawei.hms:push:${Versions.huawei_hms_push_version}"
}

object TestLibs {
    const val kotlin_test = "io.kotlintest:kotlintest-runner-junit4:${Versions.kotlin_test_version}"
    const val mockito_android = "org.mockito:mockito-android:${Versions.mockito_version}"
    const val mockito_core = "org.mockito:mockito-core:${Versions.mockito_core_version}"
    const val kotlin_mockito = "org.mockito.kotlin:mockito-kotlin:${Versions.kotlinMockito}"
    const val byte_buddy = "net.bytebuddy:byte-buddy:${Versions.byte_buddy_version}"
    const val support_test_runner = "androidx.test:runner:${Versions.support_test_version}"
    const val support_test_rules = "androidx.test:rules:${Versions.support_rules_test_version}"
    const val support_test_extensions =
        "androidx.test.ext:junit:${Versions.support_test_extensions}"
    const val support_test_fragment =
        "androidx.fragment:fragment-testing:${Versions.support_test_fragment}"
    const val multiDex = "androidx.multidex:multidex-instrumentation:${Versions.multiDexVersion}"
    const val mockk = "io.mockk:mockk-android:${Versions.mockkVersion}"
    const val mockk_agent = "io.mockk:mockk-agent-jvm:${Versions.mockkVersion}"
}