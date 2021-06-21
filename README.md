# nyris Camera View for Android
[![Build Status](https://travis-ci.org/nyris/Camera.Android.svg?branch=master)](https://travis-ci.org/nyris/Camera.Android)
[![Download](https://api.bintray.com/packages/nyris/maven/Camera.Android/images/download.svg) ](https://bintray.com/nyris/maven/Camera.Android/_latestVersion)
[![Download](https://api.bintray.com/packages/nyris/maven/ZBar.JNI/images/download.svg) ](https://bintray.com/nyris/maven/ZBar.JNI/_latestVersion)

This project is a from of the [google/cameraview](https://github.com/google/cameraview) repo.

![](nyris_logo.png)

Introduction
------
nyris is a high performance visual product search, object detection and visual recommendations engine
for retail and industry.

For more information please see [nyris.io](https://nyris.io/).

We provide a new Camera library that handles all image requirements for our matching engine.
The SDK is written in [Kotlin](https://kotlinlang.org/) and [Java](https://docs.oracle.com/javase/8/docs/technotes/guides/language/index.html).

It requires API Level 14. The library uses Camera 1 API on API Level 14-20 and Camera2 on 21 and above.

| API Level | Camera API | Preview View |
|:---------:|------------|--------------|
| 14-20     | Camera1    | TextureView  |
| 21-23     | Camera2    | TextureView  |
| 24        | Camera2    | SurfaceView  |


Features
-----
- Camera preview by placing it in a layout XML (and calling the start method)
- Configuration by attributes
  - Aspect ratio (app:aspectRatio)
  - Auto-focus (app:autoFocus)
  - Flash (app:flash)
- Auto resized picture
- Barcode reader

Installation
-----
### Java Gradle
Add the dependencies
```groovy
repositories {
    maven {
        url  "https://maven.nyris.io/maven2/maven/"
    }
}

dependencies {
    implementation 'io.nyris:camera:1.x.x' //Replace x.x by the recent version or older version
    implementation 'io.nyris:zbarjni:0.10' //Required only if you use barcode mode
    implementation "android.arch.lifecycle:extensions:1.x.x" //Optional
}
```

Get Started
-----
### Jump To
* [Usage](#usage)
* [Barcode reader](#barcode-reader)

### Usage

```xml
    <io.nyris.CameraView
        android:id="@+id/camera"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:keepScreenOn="true"
        android:adjustViewBounds="true"
        app:autoFocus="true"
        app:aspectRatio="4:3"
        app:facing="back"
        app:imageWidth="512" //size of the taken image
        app:imageHeight="512"  //size of the taken image
        app:recognition="none" //Recognition mode
        app:flash="auto"/>
```

```java
    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }
```

### Barcode reader

```xml
    <io.nyris.CameraView
        android:id="@+id/camera"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:recognition="barcode"/>
```

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        //Listen to barcode reader
        mCameraView.addBarcodeListener(barcode -> {

        });
    }
```

You can see a complete usage in the demo app.

License
=======
    Copyright 2018 nyris GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
