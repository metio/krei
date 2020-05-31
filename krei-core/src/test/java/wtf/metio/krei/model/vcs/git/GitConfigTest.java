/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.vcs.git;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GitConfig")
class GitConfigTest {

    @DisplayName("create GitConfig")
    @Test
    void shouldCreateGitConfig() {
        assertNotNull(GitConfig.builder().build());
    }

    @DisplayName("create GitConfig for single key/value pair")
    @Test
    void shouldHaveShorthandConstructor() {
        assertNotNull(GitConfig.of("key", "value"));
    }

    @DisplayName("create custom GitConfig")
    @ParameterizedTest(name = "[{index}]")
    @MethodSource("customGit")
    void shouldBuildCustomGitConfig(final ImmutableGitConfig.Builder builder) {
        assertNotNull(builder.build());
    }

    static Stream<ImmutableGitConfig.Builder> customGit() {
        return Stream.of(
                GitConfig.builder(),
                GitConfig.builder().putEntry("key", "value"),
                GitConfig.builder().putEntry("key", "value").putEntry("other", "entry")
        );
    }

}
