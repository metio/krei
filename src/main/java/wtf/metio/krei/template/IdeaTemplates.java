package wtf.metio.krei.template;

public final class IdeaTemplates {

    public static String gitignore() {
        return read("gitignore", "default.gitignore");
    }

    public static String copyright() {
        return read("copyright", "copyright.xml");
    }

    public static String copyrightProfile() {
        return read("copyright", "profile.xml");
    }

    private static String read(final String category, final String file) {
        return Templates.template("idea/" + category, "/" + file);
    }

    private IdeaTemplates() {
        // factory class
    }

}
