# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

if (ANDROID_ABI MATCHES "^armeabi-v7a$")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfloat-abi=softfp -mfpu=neon")
elseif(ANDROID_ABI MATCHES "^arm64-v8a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -ftree-vectorize")
endif()

set(CMAKE_VERBOSE_MAKEFILE on)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DSTANDALONE_DEMO_LIB \
                    -std=c++11 -fno-exceptions -fno-rtti -O2 -Wno-narrowing \
                    -fPIE")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} \
                              -Wl,--allow-multiple-definition \
                              -Wl,--whole-archive -fPIE -v")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

if(EXISTS "src/dev/cpp/image_utils_jni.cpp")
    set(curJni src/dev/cpp/image_utils_jni.cpp)
else()
    set(curJni src/main/cpp/native-lib.cpp)
endif()

add_library( # Sets the name of the library.
             ImageUtils

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${curJni} )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       ImageUtils
                       android
                       log
                       jnigraphics
                       m
                       atomic
                       z
                       ${log-lib} )