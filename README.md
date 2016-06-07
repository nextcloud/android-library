# [Nextcloud](https://nextcloud.com) Android Library 

## Introduction
Using Nextcloud Android library it will be the easiest way to communicate with Nextcloud servers.
Add this library in your project and integrate your application with Nextcloud seamlessly.

## Use Library
In the repository it is not only the library project but also the example project “sample_client”; 
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

Other branches, some supporting branches are used to aid parallel development between team members, ease tracking of features, prepare for production releases and to assist in quickly fixing live production problems. Unlike the main branches, these branches always have a limited life time, since they will be removed eventually.

The different types of branches we use:

- Branch __perNewFeature__    
- Branch  __releaseBranches__

Both of them branch off from master and must merge back into master branch through a [pull requests](https://github.com/nextcloud/android-library/pulls) in Github. Once the PR is approved and merged, the branch may be deleted.

##  License

Nextcloud Android Library is available under MIT license. See the file LICENSE.md with the full license text. 

### Third party libraries
```
Nextcloud Android Library uses Apache JackRabbit, version 2.2.5. 
Copyright (C) 2004-2010 The Apache Software Foundation. 
Licensed under Apache License, Version 2.0.
```

```
Apache JackRabbit depends on Commons HTTPClient version 3.1 and SLF4j version 1.7.5; both included also. 
Copyright (C) 2004-2010 The Apache Software Foundation. 
Licensed under Apache License, Version 2.0.
```

## Compatibility

Nextcloud Android library is valid for Android version 2.2 and up (android:minSdkVersion="8" android:targetSdkVersion="23").

Nextcloud Android library supports Nextcloud server from version 9.
