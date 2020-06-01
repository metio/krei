/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.template;

import java.util.stream.IntStream;

/**
 * Pre-defined license templates.
 */
public enum LicenseTemplates { // TODO: extract interface?

    /**
     * @see <a href="https://creativecommons.org/publicdomain/zero/1.0/">Creative Commons Zero</a>
     */
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
