package com.hycer.mirror;

public class Constants {
    public static String BACKUP_PATH = "MirrorBackup/";
    public static String WORLD_PATH = "world";
    public static String CONFIG_PATH = "config/";
    public static String CONFIG_FILE = "Mirror.json";

    static int SYSTEM_TYPE; // 1: Windows 2: Linux 3: MacOS

    static {
        SYSTEM_TYPE = setSystemType();
    }
    public static String ROLLBACK_SCRIPT_FILE =  Constants.SYSTEM_TYPE == 1 ? "rollback.bat" : "rollback.sh";
    public static String START_SCRIPT_FILE = Constants.SYSTEM_TYPE == 1 ? "start.bat" : "start.sh";

    private static int setSystemType() {
        String sysName = System.getProperty("os.name").toLowerCase();
        if (sysName.contains("win")) {
            return 1;
        } else if (sysName.contains("lin") || sysName.contains("nux") || sysName.contains("aix")) {
            return 2;
        } else {
            return 3;
        }
    }
}
