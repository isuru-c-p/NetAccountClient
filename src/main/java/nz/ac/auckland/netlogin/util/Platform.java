package nz.ac.auckland.netlogin.util;

public class Platform {

    public static boolean isMac() {
        return System.getProperty("os.name").equals("Mac OS X");
    }

}
