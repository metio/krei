package wtf.metio.krei.template;

public enum GitIgnoreTemplates {

    BAZEL("bazel"),

    MAVEN("maven");

    private final String file;

    GitIgnoreTemplates(final String file) {
        this.file = file;
    }

    public String ignores() {
        return Templates.template("gitignore", file + ".gitignore");
    }

}
