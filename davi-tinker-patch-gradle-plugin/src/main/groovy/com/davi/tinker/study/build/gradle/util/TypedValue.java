
package com.davi.tinker.study.build.gradle.util;


public class TypedValue {
    public static final int BUFFER_SIZE = 16384;

    public static final int K_BYTES = 1024;

    public static final String FILE_TXT           = ".txt";
    public static final String FILE_XML           = ".xml";
    public static final String FILE_APK           = ".apk";
    public static final String FILE_CONFIG        = "config.xml";
    public static final String FILE_LOG           = "log.txt";
    public static final String SO_LOG_FILE        = "so_log.txt";
    public static final String SO_META_FILE       = "so_meta.txt";
    public static final String DEX_LOG_FILE       = "dex_log.txt";
    public static final String DEX_META_FILE      = "dex_meta.txt";
    public static final String DEX_TEMP_PATCH_DIR = "tempPatchedDexes";
    public static final String RES_LOG_FILE       = "res_log.txt";
    public static final String RES_META_TXT       = "res_meta.txt";
    public static final String ARKHOT_META_TXT = "arkHot_meta.txt";

    public static final String FILE_ASSETS = "assets";

    public static final String TINKER_ID     = "TINKER_ID";
    public static final String NEW_TINKER_ID = "NEW_TINKER_ID";

    // Please keep it synchronized with the one defined in ShareConstants.
    public static final String PKGMETA_KEY_IS_PROTECTED_APP = "is_protected_app";

    public static final String PACKAGE_META_FILE = "package_meta.txt";

    // Please keep it synchronized with the other one defined in 'EnvConsts' class
    public static final String INCCOMPONENT_META_FILE = "assets/inc_component_meta.txt";

    public static final String PATH_DEFAULT_OUTPUT = "tinkerPatch";

    public static final String PATH_PATCH_FILES   = "tinker_result";
    public static final String OUT_7ZIP_FILE_PATH = "out_7zip";

    public static final int    ANDROID_40_API_LEVEL    = 14;
    public static final double DEX_PATCH_MAX_RATIO     = 0.6;
    public static final double DEX_JAR_PATCH_MAX_RATIO = 1.0;
    public static final double BSDIFF_PATCH_MAX_RATIO  = 0.8;

    public static final String RES_ARSC     = "resources.arsc";
    public static final String RES_MANIFEST = "AndroidManifest.xml";
    public static final String RES_OUT      = "resources_out.zip";
    public static final String RES_OUT_7ZIP = "resources_out_7z.zip";

    public static final int ADD       = 1;
    public static final int MOD       = 2;
    public static final int DEL       = 3;
    public static final int LARGE_MOD = 4;
    public static final int STORED    = 5;


    public static final String ADD_TITLE       = "add:";
    public static final String MOD_TITLE       = "modify:";
    public static final String LARGE_MOD_TITLE = "large modify:";
    public static final String DEL_TITLE       = "delete:";
    public static final String PATTERN_TITLE   = "pattern:";
    public static final String STORE_TITLE     = "store:";

}
