/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

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
