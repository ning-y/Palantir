add_library(
    tcl

    STATIC

    src/main/tcl/generic/regcomp.c
    src/main/tcl/generic/regerror.c
    src/main/tcl/generic/regexec.c
    src/main/tcl/generic/regfree.c
    src/main/tcl/generic/tclAlloc.c
    src/main/tcl/generic/tclAsync.c
    src/main/tcl/generic/tclBasic.c
    src/main/tcl/generic/tclBinary.c
    src/main/tcl/generic/tclCkalloc.c
    src/main/tcl/generic/tclClock.c
    src/main/tcl/generic/tclCmdAH.c
    src/main/tcl/generic/tclCmdIL.c
    src/main/tcl/generic/tclCmdMZ.c
    src/main/tcl/generic/tclCompCmds.c
    src/main/tcl/generic/tclCompExpr.c
    src/main/tcl/generic/tclCompile.c
    src/main/tcl/generic/tclConfig.c
    src/main/tcl/generic/tclDate.c
    src/main/tcl/generic/tclDecls.h
    src/main/tcl/generic/tclDictObj.c
    src/main/tcl/generic/tclEncoding.c
    src/main/tcl/generic/tclEnv.c
    src/main/tcl/generic/tclEvent.c
    src/main/tcl/generic/tclExecute.c
    src/main/tcl/generic/tclFCmd.c
    src/main/tcl/generic/tclFileName.c
    src/main/tcl/generic/tclGet.c
    src/main/tcl/generic/tclHash.c
    src/main/tcl/generic/tclHistory.c
    src/main/tcl/generic/tclIO.c
    src/main/tcl/generic/tclIOCmd.c
    src/main/tcl/generic/tclIOGT.c
    src/main/tcl/generic/tclIORChan.c
    src/main/tcl/generic/tclIOSock.c
    src/main/tcl/generic/tclIOUtil.c
    src/main/tcl/generic/tclIndexObj.c
    src/main/tcl/generic/tclInterp.c
    src/main/tcl/generic/tclLink.c
    src/main/tcl/generic/tclListObj.c
    src/main/tcl/generic/tclLiteral.c
    src/main/tcl/generic/tclLoad.c
    src/main/tcl/generic/tclMain.c
    src/main/tcl/generic/tclNamesp.c
    src/main/tcl/generic/tclNotify.c
    src/main/tcl/generic/tclObj.c
    src/main/tcl/generic/tclPanic.c
    src/main/tcl/generic/tclParse.c
    src/main/tcl/generic/tclPathObj.c
    src/main/tcl/generic/tclPipe.c
    src/main/tcl/generic/tclPkg.c
    src/main/tcl/generic/tclPkgConfig.c
    src/main/tcl/generic/tclPosixStr.c
    src/main/tcl/generic/tclPreserve.c
    src/main/tcl/generic/tclProc.c
    src/main/tcl/generic/tclRegexp.c
    src/main/tcl/generic/tclResolve.c
    src/main/tcl/generic/tclResult.c
    src/main/tcl/generic/tclScan.c
    src/main/tcl/generic/tclStrToD.c
    src/main/tcl/generic/tclStringObj.c
    src/main/tcl/generic/tclStubInit.c
    src/main/tcl/generic/tclStubLib.c
    src/main/tcl/generic/tclTest.c
    src/main/tcl/generic/tclTestObj.c
    src/main/tcl/generic/tclTestProcBodyObj.c
    src/main/tcl/generic/tclThread.c
    src/main/tcl/generic/tclThreadAlloc.c
    src/main/tcl/generic/tclThreadJoin.c
    src/main/tcl/generic/tclThreadStorage.c
    src/main/tcl/generic/tclTimer.c
    src/main/tcl/generic/tclTomMathInterface.c
    src/main/tcl/generic/tclTrace.c
    src/main/tcl/generic/tclUtf.c
    src/main/tcl/generic/tclUtil.c
    src/main/tcl/generic/tclVar.c
    src/main/tcl/unix/tclLoadDl.c

    src/main/tcl/libtommath/bn_fast_s_mp_mul_digs.c
    src/main/tcl/libtommath/bn_fast_s_mp_sqr.c
    src/main/tcl/libtommath/bn_mp_add.c
    src/main/tcl/libtommath/bn_mp_add_d.c
    src/main/tcl/libtommath/bn_mp_and.c
    src/main/tcl/libtommath/bn_mp_clamp.c
    src/main/tcl/libtommath/bn_mp_clear.c
    src/main/tcl/libtommath/bn_mp_clear_multi.c
    src/main/tcl/libtommath/bn_mp_cmp.c
    src/main/tcl/libtommath/bn_mp_cmp_d.c
    src/main/tcl/libtommath/bn_mp_cmp_mag.c
    src/main/tcl/libtommath/bn_mp_cnt_lsb.c
    src/main/tcl/libtommath/bn_mp_copy.c
    src/main/tcl/libtommath/bn_mp_count_bits.c
    src/main/tcl/libtommath/bn_mp_div.c
    src/main/tcl/libtommath/bn_mp_div_2.c
    src/main/tcl/libtommath/bn_mp_div_2d.c
    src/main/tcl/libtommath/bn_mp_div_3.c
    src/main/tcl/libtommath/bn_mp_div_d.c
    src/main/tcl/libtommath/bn_mp_exch.c
    src/main/tcl/libtommath/bn_mp_expt_d.c
    src/main/tcl/libtommath/bn_mp_grow.c
    src/main/tcl/libtommath/bn_mp_init.c
    src/main/tcl/libtommath/bn_mp_init_copy.c
    src/main/tcl/libtommath/bn_mp_init_multi.c
    src/main/tcl/libtommath/bn_mp_init_set.c
    src/main/tcl/libtommath/bn_mp_init_set_int.c
    src/main/tcl/libtommath/bn_mp_init_size.c
    src/main/tcl/libtommath/bn_mp_karatsuba_mul.c
    src/main/tcl/libtommath/bn_mp_karatsuba_sqr.c
    src/main/tcl/libtommath/bn_mp_lshd.c
    src/main/tcl/libtommath/bn_mp_mod.c
    src/main/tcl/libtommath/bn_mp_mod_2d.c
    src/main/tcl/libtommath/bn_mp_mul.c
    src/main/tcl/libtommath/bn_mp_mul_2.c
    src/main/tcl/libtommath/bn_mp_mul_2d.c
    src/main/tcl/libtommath/bn_mp_mul_d.c
    src/main/tcl/libtommath/bn_mp_neg.c
    src/main/tcl/libtommath/bn_mp_or.c
    src/main/tcl/libtommath/bn_mp_radix_size.c
    src/main/tcl/libtommath/bn_mp_radix_smap.c
    src/main/tcl/libtommath/bn_mp_read_radix.c
    src/main/tcl/libtommath/bn_mp_rshd.c
    src/main/tcl/libtommath/bn_mp_set.c
    src/main/tcl/libtommath/bn_mp_set_int.c
    src/main/tcl/libtommath/bn_mp_shrink.c
    src/main/tcl/libtommath/bn_mp_sqr.c
    src/main/tcl/libtommath/bn_mp_sqrt.c
    src/main/tcl/libtommath/bn_mp_sub.c
    src/main/tcl/libtommath/bn_mp_sub_d.c
    src/main/tcl/libtommath/bn_mp_to_unsigned_bin.c
    src/main/tcl/libtommath/bn_mp_to_unsigned_bin_n.c
    src/main/tcl/libtommath/bn_mp_toom_mul.c
    src/main/tcl/libtommath/bn_mp_toom_sqr.c
    src/main/tcl/libtommath/bn_mp_toradix_n.c
    src/main/tcl/libtommath/bn_mp_unsigned_bin_size.c
    src/main/tcl/libtommath/bn_mp_xor.c
    src/main/tcl/libtommath/bn_mp_zero.c
    src/main/tcl/libtommath/bn_reverse.c
    src/main/tcl/libtommath/bn_s_mp_add.c
    src/main/tcl/libtommath/bn_s_mp_mul_digs.c
    src/main/tcl/libtommath/bn_s_mp_sqr.c
    src/main/tcl/libtommath/bn_s_mp_sub.c
    src/main/tcl/libtommath/bncore.c

    src/main/tcl/unix/tclAppInit.c
    src/main/tcl/unix/tclUnixChan.c
    src/main/tcl/unix/tclUnixCompat.c
    src/main/tcl/unix/tclUnixEvent.c
    src/main/tcl/unix/tclUnixFCmd.c
    src/main/tcl/unix/tclUnixFile.c
    src/main/tcl/unix/tclUnixInit.c
    src/main/tcl/unix/tclUnixNotfy.c
    src/main/tcl/unix/tclUnixPipe.c
    src/main/tcl/unix/tclUnixSock.c
    src/main/tcl/unix/tclUnixTest.c
    src/main/tcl/unix/tclUnixThrd.c
    src/main/tcl/unix/tclUnixTime.c
)

target_include_directories(
    tcl

    PRIVATE

    src/main/tcl/generic
    src/main/tcl/libtommath
    src/main/tcl/unix
)

target_compile_definitions(
    tcl

    PRIVATE

    -DNO_UNION_WAIT  # used in unix/tclUnixPort.h
    -DSTDC_HEADERS  # used in tclInt.h
    -DCFG_INSTALL_LIBDIR="${CMAKE_CACHEFILE_DIR}"  # used in generic/tclPkgConfig.c
    -DCFG_INSTALL_BINDIR="${CMAKE_CURRENT_BINARY_DIR}"
    -DCFG_INSTALL_SCRDIR="${CMAKE_CACHEFILE_DIR}"
    -DCFG_INSTALL_INCDIR="${CMAKE_CACHEFILE_DIR}"
    -DCFG_INSTALL_DOCDIR="${CMAKE_CACHEFILE_DIR}"
    -DCFG_RUNTIME_LIBDIR="${CMAKE_CACHEFILE_DIR}"
    -DCFG_RUNTIME_BINDIR="${CMAKE_CURRENT_BINARY_DIR}"
    -DCFG_RUNTIME_SCRDIR="${CMAKE_CACHEFILE_DIR}"
    -DCFG_RUNTIME_INCDIR="${CMAKE_CACHEFILE_DIR}"
    -DCFG_RUNTIME_DOCDIR="${CMAKE_CACHEFILE_DIR}"
    -DTCL_CFGVAL_ENCODING="iso8859-1"
    -DTCL_LIBRARY="${CMAKE_CACHEFILE_DIR}"  # used in unix/tclUnixInit.c
    -DTCL_PACKAGE_PATH="${CMAKE_CACHEFILE_DIR}"
)

# TCL auxiliary files  // TODO only copy necessary files
file(
    COPY
    src/main/tcl/library/

    DESTINATION
    ${PROJECT_SOURCE_DIR}/src/main/assets/tcl_aux/
)
