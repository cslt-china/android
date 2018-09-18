load("//tools/build_defs/android:rules.bzl", "android_binary", "android_library")

package(
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "signalong_lib",
    srcs = glob([
        "*.java",
        "api/*.java",
        "utils/*.java",
        "jsonentities/*.java",
        "widget/*.java",
        "db/*.java",
        "db/dbentities/*.java",
        "db/dao/*.java",
        "repository/*.java",
        "service/*.java",
        "broadcast/*.java",
    ]),
    enable_data_binding = 1,
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = [":res"],
    deps = [
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/annotations",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/arch_components/lifecycle",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/arch_components/lifecycle/extensions",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/arch_components/paging/common",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/arch_components/paging/runtime",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/arch_components/persistence/room",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/arch_components/persistence/room/rxjava2",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/compat",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/core_ui",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/core_utils",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/databinding:runtime",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/design",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/fragment",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/transition",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v13",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v4",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v7/appcompat",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v7/recyclerview",
        "//third_party/java/android_libs/glide",
        "//third_party/java/android_libs/guava_jdk5",
        "//third_party/java/android_libs/guava_jdk5:base",
        "//third_party/java/android_libs/rxandroid2",
        "//third_party/java/dagger:android-support",
        "//third_party/java/gson",
        "//third_party/java/okhttp3:okhttp",
        "//third_party/java/okhttp3:okhttp_logginginterceptor",
        "//third_party/java/retrofit2",
        "//third_party/java/retrofit2:converter-gson",
        "//third_party/java/rxjava2",
    ],
)

android_binary(
    name = "signalong",
    manifest = "AndroidManifest.xml",
    multidex = "native",
    deps = [
        ":signalong_lib",
    ],
)

filegroup(
    name = "res",
    srcs = glob(["res/**/*"]),
)
