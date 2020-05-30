/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PrintStreams {

    public static void withPrintStream(final BiConsumer<PrintStream, ByteArrayOutputStream> consumer) {
        final var baos = new ByteArrayOutputStream();
        try (final var ps = new PrintStream(baos, true, UTF_8)) {
            consumer.accept(ps, baos);
        }
    }

    private PrintStreams() {
        // factory class
    }

}
