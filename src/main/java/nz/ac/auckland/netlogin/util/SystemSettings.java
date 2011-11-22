package nz.ac.auckland.netlogin.util;

/**
 * Created by IntelliJ IDEA.
 * User: regg002
 * Date: 22/11/11
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SystemSettings {
    public static void setSystemPropertyDefault(String name, String value) {
        if (System.getProperty(name) == null) {
            System.setProperty(name, value);
        }
    }
}
