# Set cmke min version
cmake_minimum_required(VERSION 3.4.1)

# Set complie type
SET(MODULE_NAME mp3lame)
set(CMAKE_BUILE_TYPE RELEASE)
set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -s")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

# Set include(*.h/*hpp) dir
include_directories(./ ./libmp3lame)

# Set source(*.c/*.cpp) dir
aux_source_directory(. SRC_DIR)
aux_source_directory(libmp3lame SRC_LAME)
list(APPEND SRC_DIR ${SRC_LAME})

add_library(${MODULE_NAME} SHARED ${SRC_DIR})

find_library(log-lib log)

target_link_libraries(${MODULE_NAME} ${log-lib})