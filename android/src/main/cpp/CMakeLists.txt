cmake_minimum_required(VERSION 3.18.1)

project("serial_port")

SET(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/libs)

add_library(serial_port SHARED
        SerialPort.c
        SerialPort.h)

# Include libraries needed for hello-jni lib
target_link_libraries(serial_port
        android
        log)
