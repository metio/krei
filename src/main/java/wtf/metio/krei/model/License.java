package wtf.metio.krei.model;

import wtf.metio.krei.template.license.CreateCommonsZero;

public enum License {

    CC0("cc0",
            CreateCommonsZero.TEXT,
            CreateCommonsZero.SOURCE_HEADER,
            CreateCommonsZero.SOURCE_HEADER_MATCHER,
            2,
            CreateCommonsZero.README);

    private final String id;
    private final String text;
    private final String sourceHeader;
    private final String sourceHeaderMatcher;
    private final int sourceHeaderPlaceholders;
    private final String readme;

    License(
            final String id,
            final String text,
            final String sourceHeader,
            final String sourceHeaderMatcher,
            int sourceHeaderPlaceholders,
            final String readme) {
        this.id = id;
        this.text = text;
        this.sourceHeader = sourceHeader;
        this.sourceHeaderMatcher = sourceHeaderMatcher;
        this.sourceHeaderPlaceholders = sourceHeaderPlaceholders;
        this.readme = readme;
    }

    public String id() {
        return id;
    }

    public String text() {
        return text;
    }

    public String sourceHeader() {
        return sourceHeader;
    }

    public String sourceHeaderMatcher() {
        return sourceHeaderMatcher;
    }

    public int sourceHeaderPlaceholders() {
        return sourceHeaderPlaceholders;
    }

    public String readme() {
        return readme;
    }
}
