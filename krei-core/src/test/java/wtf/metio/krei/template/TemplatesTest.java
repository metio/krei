/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplatesTest {

    @Test
    void shouldReadTemplate() {
        // given
        String category = "idea/gitignore/";
        String file = "default.gitignore";

        // when
        final var template = Templates.template(category, file);

        // then
        assertNotNull(template);
        assertFalse(template.isBlank());
    }

    @Test
    void shouldReturnEmptyStringForNonExistingTemplate() {
        // given
        String category = "category";
        String file = "file";

        // when
        final var template = Templates.template(category, file);

        // then
        assertNotNull(template);
        assertTrue(template.isBlank());
    }

}
