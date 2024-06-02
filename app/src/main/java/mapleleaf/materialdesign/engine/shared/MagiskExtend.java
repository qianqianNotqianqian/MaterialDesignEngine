package mapleleaf.materialdesign.engine.shared;

import java.io.File;

import mapleleaf.materialdesign.engine.shell.KeepShellPublic;
import mapleleaf.materialdesign.engine.shell.RootFile;

public class MagiskExtend {
    // source /data/adb/util_functions.sh

    public static String MAGISK_PATH = "/sbin/.core/img/scene_systemless/";
    private static String MAGISK_PATH_19 = "/data/adb/modules"; //  "/sbin/.magisk/modules";
    private static String MAGISK_ROOT_PATH1 = "/sbin/.core/img";
    private static String MAGISK_ROOT_PATH2 = "/sbin/.magisk/img";

    private static String MAGISK_MODULE_NAME = "scene_systemless";
    //magisk 19 /data/adb/modules
    private static int supported = -1;
    private static int MagiskVersion = 0;

    // 递归方式 计算文件的大小
    private static long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }

    /**
     * 自动调整镜像大小
     */
    private static boolean spaceValidation(long require) {
        // magisk 19开始，不用镜像了，理论上空间无限
        if (MagiskVersion >= 19 || MAGISK_PATH.startsWith("/data")) {
            return true;
        }

        long space = new File(MAGISK_PATH).getFreeSpace();
        // 镜像空间不足
        if (space < (require + 4096)) {
            return false;
        }

        return true;
    }

    /**
     * 是否已经安装magisk并且版本合适
     *
     * @return 是否已安装
     */
    public static boolean magiskSupported() {
        if (supported == -1 || MagiskVersion < 1) {
            String magiskVersion = KeepShellPublic.INSTANCE.doCmdSync("magisk -V");
            if (!magiskVersion.equals("error")) {
                try {
                    MagiskVersion = Integer.parseInt(magiskVersion) / 1000;
                    supported = MagiskVersion >= 17 ? 1 : 0;

                    if (supported == 1) {
                        if (MagiskVersion >= 19) {
                            MAGISK_PATH = MAGISK_PATH_19 + "/" + MAGISK_MODULE_NAME + "/";
                        } else if (RootFile.INSTANCE.dirExists(MAGISK_ROOT_PATH1)) {
                            MAGISK_PATH = MAGISK_ROOT_PATH1 + "/" + MAGISK_MODULE_NAME + "/";
                        } else if (RootFile.INSTANCE.dirExists(MAGISK_ROOT_PATH2)) {
                            MAGISK_PATH = MAGISK_ROOT_PATH2 + "/" + MAGISK_MODULE_NAME + "/";
                        }
                    }
                } catch (Exception ignored) {
                }
            } else {
                supported = 0;
            }
        }
        return supported == 1;
    }

    /**
     * 是否已安装模块
     */
    public static boolean moduleInstalled() {
        return magiskSupported() && RootFile.INSTANCE.fileExists(MAGISK_PATH + "module.prop");
    }

    public static boolean setSystemProp(String prop, String value) {
        if (!spaceValidation(prop.length() + 4 + value.length())) {
            return false;
        }

        KeepShellPublic.INSTANCE.doCmdSync(
                "sed -i '/" + prop + "=/'d " + MAGISK_PATH + "system.prop\n" +
                        "echo " + prop + "=\"" + value + "\" >> " + MAGISK_PATH + "system.prop\n");
        return true;
    }

    public static void deleteSystemPath(String orginPath) {
        if (RootFile.INSTANCE.itemExists(orginPath)) {
            String output = getMagiskReplaceFilePath(orginPath);
            String dir = new File(output).getParent();
            KeepShellPublic.INSTANCE.doCmdSync("mkdir -p \"" + dir + "\"\necho '' > \"" + output + "\"");
        }
    }

    public static boolean replaceSystemFile(String orginPath, String newfile) {
        if (spaceValidation(getTotalSizeOfFilesInDir(new File(newfile)))) {
            if (RootFile.INSTANCE.itemExists(newfile)) {
                String output = getMagiskReplaceFilePath(orginPath);
                String dir = new File(output).getParent();
                KeepShellPublic.INSTANCE.doCmdSync(
                        "mkdir -p \"" + dir + "\"\n" +
                                "cp \"" + newfile + "\" \"" + output + "\"\n" +
                                "chmod 777 \"" + output + "\"");
                return true;
            }
        }

        return false;
    }

    public static String getMagiskReplaceFilePath(String systemPath) {
        return MAGISK_PATH.substring(0, MAGISK_PATH.length() - 1) + ((systemPath.startsWith("/vendor") || systemPath.startsWith("/product")) ? ("/system" + systemPath) : systemPath);
    }

}
