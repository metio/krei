package wtf.metio.krei.io;

import wtf.metio.krei.model.License;

public final class Licenses {

    public static String header(final License license) {
        return read(license, "/HEADER");
    }

    public static String license(final License license) {
        return read(license, "/LICENSE");
    }

    public static String matcher(final License license) {
        return read(license, "/MATCHER");
    }

    public static String readme(final License license) {
        return read(license, "/README");
    }

    public static String waiver(final License license) {
        return read(license, "/WAIVER");
    }

    private static String read(final License license, final String file) {
        return Templates.template("license", license.id() + file);
    }

    private Licenses() {
        // factory class
    }

}
