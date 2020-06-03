object Versions {
    const val kotlin_version = "1.3.72"
    const val kotlin_test_version = "3.1.11"
    const val mockito_version = "2.25.0"
    const val espresso_version = "3.2.0"
    const val firebase_core_version = "17.4.0"
    const val firebase_messaging_version = "20.1.6"
    const val hamcrest_version = "2.2"
    const val support_test_version = "1.2.0"
    const val support_library = "1.1.0"
    const val support_annotations = "1.1.0"
    const val buildToolsVersion = "29.0.2"
    const val multiDexVersion = "2.0.0"
    const val androidJunit4 = "1.1.1"
    const val kotlinMockito = "2.2.0"
    const val location_services_version = "17.0.0"
}

object Libs {
    const val support_annotations = "androidx.annotation:annotation:${Versions.support_annotations}"
    const val support_appcompat = "androidx.appcompat:appcompat:${Versions.support_library}"
    const val firebase_core = "com.google.firebase:firebase-core:${Versions.firebase_core_version}"
    const val firebase_messaging = "com.google.firebase:firebase-messaging:${Versions.firebase_messaging_version}"
    const val espresso_idling_resources = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso_version}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_version}"
    const val location_services = "com.google.android.gms:play-services-location:${Versions.location_services_version}"
}

object TestLibs {
    const val kotlin_test = "io.kotlintest:kotlintest-runner-junit4:${Versions.kotlin_test_version}"
    const val mockito_android = "org.mockito:mockito-android:${Versions.mockito_version}"
    const val kotlin_mockito = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.kotlinMockito}"
    const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_version}"
    const val support_test_runner = "androidx.test:runner:${Versions.support_test_version}"
    const val support_test_rules = "androidx.test:rules:${Versions.support_test_version}"
    const val hamcrest_core = "org.hamcrest:hamcrest-core:${Versions.hamcrest_version}"
    const val hamcrest_library = "org.hamcrest:hamcrest-library:${Versions.hamcrest_version}"
    const val multiDex = "androidx.multidex:multidex-instrumentation:${Versions.multiDexVersion}"
    const val androidJunit4 = "androidx.test.ext:junit:${Versions.androidJunit4}"
}