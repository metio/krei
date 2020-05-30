/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.test;

public final class FileMasking {

    public static String mask(final String original) {
        return original.replaceAll("/tmp/junit[0-9]+", "/tmp/junit");
    }

    private FileMasking() {
        // factory class
    }

}
