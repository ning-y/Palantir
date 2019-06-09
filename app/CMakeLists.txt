cmake_minimum_required(VERSION 3.6.0)

include(TCL.cmake)
include(VMD.cmake)
include(molfile.cmake)

target_link_libraries(vmd tcl)