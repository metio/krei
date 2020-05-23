package wtf.metio.krei.test;

public final class FileMasking {

    public static String mask(final String original) {
        return original.replaceAll("/tmp/junit[0-9]+", "/tmp/junit");
    }

    private FileMasking() {
        // factory class
    }

}
