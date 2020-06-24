# Set cmke min version
cmake_minimum_required(VERSION 3.4.1)

# Set complie type
set(CMAKE_BUILE_TYPE RELEASE)
set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -s")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

# Set abi output dir
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# Set include(*.h/*hpp) dir
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libmp3lame)

# Set source(*.c/*.cpp) dir
aux_source_directory(${CMAKE_CURRENT_SOURCE_DIR} SRC_DIR)
aux_source_directory(${CMAKE_CURRENT_SOURCE_DIR}/libmp3lame SRC_LAME)
list(APPEND SRC_DIR ${SRC_LAME})

# Set source(*.h/*.cpp/*.c/*.cpp) dir
#file(GLOB SRC_DIR
#        *.h
#        *.cpp
#        libmp3lame/*.h
#        libmp3lame/*c
#)

add_library(
        # Sets the name of the library.
        mp3lame

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SRC_DIR}
)

find_library(
        # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
)

target_link_libraries(
        # Specifies the target library.
        mp3lame

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
)