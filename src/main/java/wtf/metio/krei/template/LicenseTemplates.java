package wtf.metio.krei.template;

import java.util.stream.IntStream;

public enum LicenseTemplates {

    CC0("cc0", 2);

    private final String id;
    private final int sourceHeaderPlaceholders;

    LicenseTemplates(final String id, int sourceHeaderPlaceholders) {
        this.id = id;
        this.sourceHeaderPlaceholders = sourceHeaderPlaceholders;
    }

    public String id() {
        return id;
    }

    public String header(final String projectName) {
        final var args = IntStream.range(0, sourceHeaderPlaceholders)
                .mapToObj(i -> projectName)
                .toArray(String[]::new);
        return String.format(read("/HEADER"), args);
    }

    public String license() {
        return read("/LICENSE");
    }

    public String matcher(final String projectName) {
        return String.format(read("/MATCHER"), projectName);
    }

    public String readme() {
        return read("/README");
    }

    public String waiver(final String projectName) {
        return String.format(read("/WAIVER"), projectName);
    }

    private String read(final String file) {
        return Templates.template("license", id + file);
    }

}
