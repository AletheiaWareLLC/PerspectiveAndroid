![Perspective](./app/src/main/res/drawable/banner.png)

PerspectiveAndroid
==================

This is an Android implementation of Perspective - 3D puzzle game.

Prerequisites
=============

* Android SDK usually as part of [AndroidStudio](https://developer.android.com/studio).
Specifically the location of the android sdk/ndk must be known. Either add the ANDROID_SDK_ROOT
environment variable `export ANDROID_SDK_ROOT=<path_to_sdk>` or as part of  `local.properties`:
```
ndk.dir=<path_to_ndk>
sdk.dir=<path_to_sdk>
```

Setup
=====

Clone Perspective, PerspectiveAndroid, which is this repo,  and PerspectiveJava
in to the same directory:

```
<parent>
|- Perspective
|- PerspectiveAndroid
|- PerspectiveJava
```

Now change your current directory to PerspectiveAndroid

```
cd PerspectiveAndroid
```

if when executing ./gradlew or gradle and you see error:
```
Could not open terminal for stdout: Could not get termcap entry
```

It means your linux distributation, such as Arch Linux, is not using `termcap`.
A workaround is to `unset TERM` or `export TERM=xterm`. For more information
see [github issue 4426](https://github.com/gradle/gradle/issues/4426) and/or
[this gradle discuss post](https://discuss.gradle.org/t/issue-could-not-open-terminal-for-stdout-could-not-get-termcap-entry/26902).

Build
=====
Build debug and release version

```
./gradlew build
```

Clean
=====
Clean all versions

```
./gradlew clean
```

Install
=======

Install debug version

```
./gradlew InstallDebug
```

Installs and runs the test for debug

```
./gradlew connectedDebugAndroidTest
```

Info
====

To see the list of tasks

```
./gradlew tasks
```
