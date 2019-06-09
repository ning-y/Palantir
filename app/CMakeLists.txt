cmake_minimum_required(VERSION 3.6.0)

include(VMD.cmake)
include(STRIDE.cmake)
include(TCL.cmake)
include(molfile.cmake)

target_link_libraries(vmd tcl)