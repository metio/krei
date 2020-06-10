/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdeaTemplatesTest {

    @Test
    void shouldCreateGitIgnore() {
        // given
        // when
        final var gitignore = IdeaTemplates.gitignore();

        // then
        assertNotNull(gitignore);
    }

    @Test
    void shouldCreateCopyright() {
        // given
        // when
        final var copyright = IdeaTemplates.copyright();

        // then
        assertNotNull(copyright);
    }

    @Test
    void shouldCreateCopyrightProfile() {
        // given
        // when
        final var copyrightProfile = IdeaTemplates.copyrightProfile();

        // then
        assertNotNull(copyrightProfile);
    }

}
