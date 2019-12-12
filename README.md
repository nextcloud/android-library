# [Nextcloud](https://nextcloud.com) Android Library v2 [![Build Status](https://drone.nextcloud.com/api/badges/nextcloud/android-library/status.svg)](https://drone.nextcloud.com/nextcloud/android-library) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/d9f94f04e0f447a6b21c0ae08f6f7594)](https://www.codacy.com/app/Nextcloud/android-library?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nextcloud/android-library&amp;utm_campaign=Badge_Grade)

## Introduction
Using Nextcloud Android library it will be the easiest way to communicate with Nextcloud servers.
Add this library in your project and integrate your application with Nextcloud seamlessly.

## Android Library v2
Starting from 01.10.2019 we will not actively develop our old library (v1), but maintain it until 01.10.2021 with bug fixes.
v2 is using [OkHTTP](https://square.github.io/okhttp) and [DAV4jvm](https://gitlab.com/bitfireAT/dav4jvm) by [BitfireAT](https://www.bitfire.at/).
Needed changes for projects using this library are:
- change build.gradle
  - add to android {…}: compileOptions {
  ```
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
  ```
    }
  -  add to dependencies {…}:
  ```
     implementation "commons-httpclient:commons-httpclient:3.1@jar" // remove after entire switch to lib v2
  ``` 

## Use Library
In the repository it is not only the library project but also the example project "sample_client"; 
thanks to it you will learn how to use the library.

*There are different ways of adding this library to your code*

### Gradle / Maven dependency
At the moment we do not have a publishing mechanism to a maven repository so the easiest way to add the library to your app is via a JitPack Dependency [![](https://jitpack.io/v/nextcloud/android-library.svg)](https://jitpack.io/#nextcloud/android-library)

```
repositories {
    ...
    maven { url "https://jitpack.io" }
}
dependencies {
    ...
    compile 'com.github.nextcloud:android-library:-SNAPSHOT'
```

### As a git submodule
Basically get this code and compile it having it integrated via a git submodule:

1. go into your own apps directory on the command line and add this lib as a submodule: ```git submodule add https://github.com/nextcloud/android-library nextcloud-android-library```
2. Import/Open your app in Android Studio

##  Branching strategy
The repository holds one main branch with an infinite lifetime:

- master 

Branch __origin/master__ is considered the main branch where the source code of HEAD always reflects a state with the latest delivered development changes for the next release.

Other branches, some supporting branches are used to aid parallel development between team members, ease tracking of features and to assist in quickly fixing live production problems. Unlike the main branch, these branches always have a limited life time, since they will be removed eventually (feature branching).

## Development process
We are all about quality while not sacrificing speed so we use a very pragmatic workflow.

* create an issue with feature request
    * discuss it with other developers 
    * create mockup if necessary
    * must be approved --> label approved
    * after that no conceptual changes!
* develop code
* create [pull request](https://github.com/nextcloud/android-library/pulls)
* to assure the quality of the app, any PR gets reviewed, approved and tested by [two developers](https://github.com/nextcloud/android-library/blob/master/MAINTAINERS) before it will be merged to master

##  License

Nextcloud Android Library is available under MIT license. See [LICENSE.md](https://github.com/nextcloud/android-library/blob/master/LICENSE.md) with the full license text. 

### Third party libraries
```
Nextcloud Android Library uses Apache JackRabbit, version 2.12.4. 
Copyright (C) 2004-2010 The Apache Software Foundation. 
Licensed under Apache License, Version 2.0.
```

```
Apache JackRabbit depends on Commons HTTPClient version 3.1 and SLF4j version 1.7.5; both included also. 
Copyright (C) 2004-2010 The Apache Software Foundation. 
Licensed under Apache License, Version 2.0.
```

## Compatibility

Nextcloud Android library is valid for Android version 3.0 and up (with ```android:minSdkVersion="11"``` and ```android:targetSdkVersion="24"```).

Nextcloud Android library supports Nextcloud server from version 9+.

When using newer libraries in your application that integrates with the Nextcloud Android library you could hit a conflict issue with the logging libraries used by our (outdated) HTTPClient. In order to mitigate the issues, please add the following in your build.gradle for the time being:

```
configurations.all {
    exclude group: "org.slf4j", module: "slf4j-log4j12"
    exclude group: "log4j", module: "log4j"
}
```
