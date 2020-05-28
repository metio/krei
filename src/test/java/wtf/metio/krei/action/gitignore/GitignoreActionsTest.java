/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.gitignore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wtf.metio.krei.model.Action;
import wtf.metio.krei.test.FileMasking;
import wtf.metio.krei.test.TestResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

class GitignoreActionsTest {

    @DisplayName("produce expected .gitignore")
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("gitignores")
    void shouldProduceExpectedScript(
            final String fileName,
            final Function<Path, Action> action,
            @TempDir final Path projectDirectory) throws IOException {
        // given
        final var expectedText = TestResources.read(fileName, GitignoreActionsTest.class);
        final var gitignore = projectDirectory.resolve(".gitignore");

        // when
        action.apply(gitignore).call();
        final var text = FileMasking.mask(Files.readString(gitignore, UTF_8));

        // then
        assertEquals(expectedText, text);
    }

    static Stream<Arguments> gitignores() {
        return Stream.of(
                of("bazel.gitignore", BAZEL),
                of("maven.gitignore", MAVEN)
        );
    }

    static final Function<Path, Action> BAZEL = (Path dir) -> GitignoreActions.ignore(dir, "bazel");
    static final Function<Path, Action> MAVEN = (Path dir) -> GitignoreActions.ignore(dir, "maven");

}
