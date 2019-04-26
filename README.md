![Perspective](./perspective.svg)

PerspectiveAndroid
============

This is an Android implementation of Perspective - blockchain-backed, 3D puzzle game.

Setup
=====
Libraries

    mkdir app/libs
    ln -s <bcjavalib> app/libs/BCJava.jar
    ln -s <aliasjavalib> app/libs/AliasJava.jar
    ln -s <financejavalib> app/libs/FinanceJava.jar
    ln -s <perspectivejavalib> app/libs/PerspectiveJava.jar
    ln -s <bcandroidaar> bc-android/app-debug.aar
    ln -s <joyandroidaar> joy-android/app-debug.aar

Build
=====

    ./gradlew build
