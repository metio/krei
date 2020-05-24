package wtf.metio.krei.model;

public enum License {

    CC0("cc0", 2);

    private final String id;
    private final int sourceHeaderPlaceholders;

    License(final String id, int sourceHeaderPlaceholders) {
        this.id = id;
        this.sourceHeaderPlaceholders = sourceHeaderPlaceholders;
    }

    public String id() {
        return id;
    }

    public int sourceHeaderPlaceholders() {
        return sourceHeaderPlaceholders;
    }

}
