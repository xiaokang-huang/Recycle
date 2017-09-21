# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             lib-plc

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/snap7/core/s7_text.cpp
             src/main/cpp/snap7/core/s7_peer.cpp
             src/main/cpp/snap7/core/s7_partner.cpp
             src/main/cpp/snap7/core/s7_server.cpp
             src/main/cpp/snap7/core/s7_isotcp.cpp
             src/main/cpp/snap7/core/s7_micro_client.cpp
             src/main/cpp/snap7/core/s7_client.cpp
             src/main/cpp/snap7/sys/snap_sysutils.cpp
             src/main/cpp/snap7/sys/snap_threads.cpp
             src/main/cpp/snap7/sys/snap_tcpsrvr.cpp
             src/main/cpp/snap7/sys/snap_msgsock.cpp
             src/main/cpp/snap7/lib/snap7_libmain.cpp
             src/main/cpp/snap7/snap7.cpp
             src/main/cpp/libplc-jni.cpp )

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
                       lib-plc

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

include_directories(src/main/cpp/snap7/lib/)
include_directories(src/main/cpp/snap7/sys/)
include_directories(src/main/cpp/snap7/core/)

find_package (Threads)
target_link_libraries (lib-plc ${CMAKE_THREAD_LIBS_INIT})
