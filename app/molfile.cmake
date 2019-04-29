add_library(
    molfile

    SHARED

    src/main/vmd/plugins/molfile_plugin/src/abinitplugin.c
    src/main/vmd/plugins/molfile_plugin/src/avsplugin.C
    src/main/vmd/plugins/molfile_plugin/src/babelplugin.c
    src/main/vmd/plugins/molfile_plugin/src/basissetplugin.c
    src/main/vmd/plugins/molfile_plugin/src/bgfplugin.C
    src/main/vmd/plugins/molfile_plugin/src/binposplugin.c
    src/main/vmd/plugins/molfile_plugin/src/biomoccaplugin.C
    src/main/vmd/plugins/molfile_plugin/src/brixplugin.C
    src/main/vmd/plugins/molfile_plugin/src/carplugin.c
    src/main/vmd/plugins/molfile_plugin/src/ccp4plugin.C
    src/main/vmd/plugins/molfile_plugin/src/corplugin.c
    src/main/vmd/plugins/molfile_plugin/src/cpmdplugin.c
    src/main/vmd/plugins/molfile_plugin/src/crdplugin.c
    src/main/vmd/plugins/molfile_plugin/src/cubeplugin.C
    src/main/vmd/plugins/molfile_plugin/src/dcdplugin.c
    src/main/vmd/plugins/molfile_plugin/src/dlpolyplugin.c
    src/main/vmd/plugins/molfile_plugin/src/dsn6plugin.C
    src/main/vmd/plugins/molfile_plugin/src/dxplugin.C
    src/main/vmd/plugins/molfile_plugin/src/edmplugin.C
    src/main/vmd/plugins/molfile_plugin/src/endianswap.h
    src/main/vmd/plugins/molfile_plugin/src/fastio.h
    src/main/vmd/plugins/molfile_plugin/src/fortread.h
    src/main/vmd/plugins/molfile_plugin/src/fs4plugin.C
    src/main/vmd/plugins/molfile_plugin/src/gamessplugin.c
    src/main/vmd/plugins/molfile_plugin/src/graspplugin.C
    src/main/vmd/plugins/molfile_plugin/src/grdplugin.C
    src/main/vmd/plugins/molfile_plugin/src/gridplugin.C
    src/main/vmd/plugins/molfile_plugin/src/Gromacs.h
    src/main/vmd/plugins/molfile_plugin/src/gromacsplugin.C
    src/main/vmd/plugins/molfile_plugin/src/jsplugin.c
    src/main/vmd/plugins/molfile_plugin/src/lammpsplugin.c
    src/main/vmd/plugins/molfile_plugin/src/largefiles.h
    src/main/vmd/plugins/molfile_plugin/src/main.c
    src/main/vmd/plugins/molfile_plugin/src/mapplugin.C
    src/main/vmd/plugins/molfile_plugin/src/mdfplugin.C
    src/main/vmd/plugins/molfile_plugin/src/mmcif.C
    src/main/vmd/plugins/molfile_plugin/src/mol2plugin.C
    src/main/vmd/plugins/molfile_plugin/src/moldenplugin.c
    src/main/vmd/plugins/molfile_plugin/src/molemeshplugin.C
    src/main/vmd/plugins/molfile_plugin/src/msmsplugin.C
    src/main/vmd/plugins/molfile_plugin/src/namdbinplugin.c
    src/main/vmd/plugins/molfile_plugin/src/offplugin.C
    src/main/vmd/plugins/molfile_plugin/src/parm7plugin.C
    src/main/vmd/plugins/molfile_plugin/src/parmplugin.C
    src/main/vmd/plugins/molfile_plugin/src/pbeqplugin.C
    src/main/vmd/plugins/molfile_plugin/src/pdbplugin.c
    src/main/vmd/plugins/molfile_plugin/src/pdbxplugin.C
    src/main/vmd/plugins/molfile_plugin/src/periodic_table.h
    src/main/vmd/plugins/molfile_plugin/src/phiplugin.C
    src/main/vmd/plugins/molfile_plugin/src/pltplugin.C
    src/main/vmd/plugins/molfile_plugin/src/ply.c
    src/main/vmd/plugins/molfile_plugin/src/ply.h
    src/main/vmd/plugins/molfile_plugin/src/plyplugin.C
    src/main/vmd/plugins/molfile_plugin/src/pqrplugin.c
    src/main/vmd/plugins/molfile_plugin/src/psfplugin.c
    src/main/vmd/plugins/molfile_plugin/src/qmplugin.h
    src/main/vmd/plugins/molfile_plugin/src/raster3dplugin.C
    src/main/vmd/plugins/molfile_plugin/src/ReadPARM7.h
    src/main/vmd/plugins/molfile_plugin/src/ReadPARM.h
    src/main/vmd/plugins/molfile_plugin/src/readpdb.h
    src/main/vmd/plugins/molfile_plugin/src/rst7plugin.c
    src/main/vmd/plugins/molfile_plugin/src/situsplugin.C
    src/main/vmd/plugins/molfile_plugin/src/spiderplugin.C
    src/main/vmd/plugins/molfile_plugin/src/stlplugin.C
    src/main/vmd/plugins/molfile_plugin/src/tinkerplugin.c
    src/main/vmd/plugins/molfile_plugin/src/uhbdplugin.C
    src/main/vmd/plugins/molfile_plugin/src/unit_conversion.h
    src/main/vmd/plugins/molfile_plugin/src/vasp5xdatcarplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vaspchgcarplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vaspoutcarplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vaspparchgplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vaspplugin.h
    src/main/vmd/plugins/molfile_plugin/src/vaspposcarplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vaspxdatcarplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vaspxmlplugin.c
    src/main/vmd/plugins/molfile_plugin/src/vmddir.h
    src/main/vmd/plugins/molfile_plugin/src/vtkplugin.C
    src/main/vmd/plugins/molfile_plugin/src/xbgfplugin.C
    src/main/vmd/plugins/molfile_plugin/src/xsfplugin.C
    src/main/vmd/plugins/molfile_plugin/src/xyzplugin.c
)

target_include_directories(
    molfile

    PRIVATE

    src/main/vmd/plugins/include
    src/main/vmd/plugins/molfile_plugin/src
)

target_compile_definitions(
    molfile

    PRIVATE

    -DVMDPLUGIN_STATIC  # used in src/inthash.h
)

install(
    TARGETS molfile
    LIBRARY DESTINATION src/main/assets/molfile_plugin
)
