# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/home/tranv/esp/esp-idf/components/bootloader/subproject"
  "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader"
  "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix"
  "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix/tmp"
  "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix/src/bootloader-stamp"
  "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix/src"
  "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix/src/bootloader-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix/src/bootloader-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/home/tranv/Workspace/quan-ly-giao-thong/esp32cam_security_gate/build/bootloader-prefix/src/bootloader-stamp${cfgdir}") # cfgdir has leading slash
endif()
