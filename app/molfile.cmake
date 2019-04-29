set(MOLFILE_SRC_DIR src/main/vmd/plugins/molfile_plugin/src)

add_library(abinitplugin SHARED ${MOLFILE_SRC_DIR}/abinitplugin.c)
add_library(avsplugin SHARED ${MOLFILE_SRC_DIR}/avsplugin.C)
add_library(babelplugin SHARED ${MOLFILE_SRC_DIR}/babelplugin.c)
add_library(basissetplugin SHARED ${MOLFILE_SRC_DIR}/basissetplugin.c)
add_library(bgfplugin SHARED ${MOLFILE_SRC_DIR}/bgfplugin.C)
add_library(binposplugin SHARED ${MOLFILE_SRC_DIR}/binposplugin.c)
add_library(biomoccaplugin SHARED ${MOLFILE_SRC_DIR}/biomoccaplugin.C)
add_library(brixplugin SHARED ${MOLFILE_SRC_DIR}/brixplugin.C)
add_library(carplugin SHARED ${MOLFILE_SRC_DIR}/carplugin.c)
add_library(ccp4plugin SHARED ${MOLFILE_SRC_DIR}/ccp4plugin.C)
add_library(corplugin SHARED ${MOLFILE_SRC_DIR}/corplugin.c)
add_library(cpmdplugin SHARED ${MOLFILE_SRC_DIR}/cpmdplugin.c)
add_library(crdplugin SHARED ${MOLFILE_SRC_DIR}/crdplugin.c)
add_library(cubeplugin SHARED ${MOLFILE_SRC_DIR}/cubeplugin.C)
add_library(dcdplugin SHARED ${MOLFILE_SRC_DIR}/dcdplugin.c)
add_library(dlpolyplugin SHARED ${MOLFILE_SRC_DIR}/dlpolyplugin.c)
add_library(dsn6plugin SHARED ${MOLFILE_SRC_DIR}/dsn6plugin.C)
add_library(dxplugin SHARED ${MOLFILE_SRC_DIR}/dxplugin.C)
add_library(edmplugin SHARED ${MOLFILE_SRC_DIR}/edmplugin.C)
add_library(fs4plugin SHARED ${MOLFILE_SRC_DIR}/fs4plugin.C)
add_library(gamessplugin SHARED ${MOLFILE_SRC_DIR}/gamessplugin.c)
add_library(graspplugin SHARED ${MOLFILE_SRC_DIR}/graspplugin.C)
add_library(grdplugin SHARED ${MOLFILE_SRC_DIR}/grdplugin.C)
add_library(gridplugin SHARED ${MOLFILE_SRC_DIR}/gridplugin.C)
add_library(gromacsplugin SHARED ${MOLFILE_SRC_DIR}/gromacsplugin.C)
add_library(jsplugin SHARED ${MOLFILE_SRC_DIR}/jsplugin.c)
add_library(lammpsplugin SHARED ${MOLFILE_SRC_DIR}/lammpsplugin.c)
add_library(mapplugin SHARED ${MOLFILE_SRC_DIR}/mapplugin.C)
add_library(mdfplugin SHARED ${MOLFILE_SRC_DIR}/mdfplugin.C)
add_library(mol2plugin SHARED ${MOLFILE_SRC_DIR}/mol2plugin.C)
add_library(moldenplugin SHARED ${MOLFILE_SRC_DIR}/moldenplugin.c)
add_library(molemeshplugin SHARED ${MOLFILE_SRC_DIR}/molemeshplugin.C)
add_library(msmsplugin SHARED ${MOLFILE_SRC_DIR}/msmsplugin.C)
add_library(namdbinplugin SHARED ${MOLFILE_SRC_DIR}/namdbinplugin.c)
add_library(offplugin SHARED ${MOLFILE_SRC_DIR}/offplugin.C)
add_library(parm7plugin SHARED ${MOLFILE_SRC_DIR}/parm7plugin.C)
add_library(parmplugin SHARED ${MOLFILE_SRC_DIR}/parmplugin.C)
add_library(pbeqplugin SHARED ${MOLFILE_SRC_DIR}/pbeqplugin.C)
add_library(pdbplugin SHARED ${MOLFILE_SRC_DIR}/pdbplugin.c)
add_library(phiplugin SHARED ${MOLFILE_SRC_DIR}/phiplugin.C)
add_library(pltplugin SHARED ${MOLFILE_SRC_DIR}/pltplugin.C)
add_library(pqrplugin SHARED ${MOLFILE_SRC_DIR}/pqrplugin.c)
add_library(psfplugin SHARED ${MOLFILE_SRC_DIR}/psfplugin.c)
add_library(raster3dplugin SHARED ${MOLFILE_SRC_DIR}/raster3dplugin.C)
add_library(rst7plugin SHARED ${MOLFILE_SRC_DIR}/rst7plugin.c)
add_library(situsplugin SHARED ${MOLFILE_SRC_DIR}/situsplugin.C)
add_library(spiderplugin SHARED ${MOLFILE_SRC_DIR}/spiderplugin.C)
add_library(stlplugin SHARED ${MOLFILE_SRC_DIR}/stlplugin.C)
add_library(tinkerplugin SHARED ${MOLFILE_SRC_DIR}/tinkerplugin.c)
add_library(uhbdplugin SHARED ${MOLFILE_SRC_DIR}/uhbdplugin.C)
add_library(vasp5xdatcarplugin SHARED ${MOLFILE_SRC_DIR}/vasp5xdatcarplugin.c)
add_library(vaspchgcarplugin SHARED ${MOLFILE_SRC_DIR}/vaspchgcarplugin.c)
add_library(vaspoutcarplugin SHARED ${MOLFILE_SRC_DIR}/vaspoutcarplugin.c)
add_library(vaspparchgplugin SHARED ${MOLFILE_SRC_DIR}/vaspparchgplugin.c)
add_library(vaspposcarplugin SHARED ${MOLFILE_SRC_DIR}/vaspposcarplugin.c)
add_library(vaspxdatcarplugin SHARED ${MOLFILE_SRC_DIR}/vaspxdatcarplugin.c)
add_library(vaspxmlplugin SHARED ${MOLFILE_SRC_DIR}/vaspxmlplugin.c)
add_library(xbgfplugin SHARED ${MOLFILE_SRC_DIR}/xbgfplugin.C)
add_library(xsfplugin SHARED ${MOLFILE_SRC_DIR}/xsfplugin.C)
add_library(xyzplugin SHARED ${MOLFILE_SRC_DIR}/xyzplugin.c)

include_directories(
    molfile

    PRIVATE

    src/main/vmd/plugins/include
    src/main/vmd/plugins/molfile_plugin/src
)

add_compile_options(
    -DVMDPLUGIN_STATIC  # used in src/inthash.h
)

install(
    TARGETS
        abinitplugin avsplugin babelplugin basissetplugin bgfplugin
        binposplugin biomoccaplugin brixplugin carplugin ccp4plugin
        corplugin cpmdplugin crdplugin cubeplugin dcdplugin dlpolyplugin
        dsn6plugin dxplugin edmplugin fs4plugin gamessplugin graspplugin
        grdplugin gridplugin gromacsplugin jsplugin lammpsplugin mapplugin
        mdfplugin mol2plugin moldenplugin molemeshplugin msmsplugin
        namdbinplugin offplugin parm7plugin parmplugin pbeqplugin pdbplugin
        phiplugin pltplugin pqrplugin psfplugin raster3dplugin rst7plugin
        situsplugin spiderplugin stlplugin tinkerplugin uhbdplugin
        vasp5xdatcarplugin vaspchgcarplugin vaspoutcarplugin
        vaspparchgplugin vaspposcarplugin vaspxdatcarplugin
        vaspxmlplugin xbgfplugin xsfplugin

    LIBRARY DESTINATION src/main/assets/molfile_plugin
)
