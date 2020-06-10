/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.validate;

import org.junit.jupiter.api.Test;
import wtf.metio.krei.model.ProjectConfig;

import static org.junit.jupiter.api.Assertions.*;

class ValidateTest {

    @Test
    void shouldValidateProjectConfig() {
        // given
        final var config = ProjectConfig.builder("test").build();
        
        // when
        final var problems = Validate.validate(config);

        // then
        assertFalse(problems.isEmpty());
    }

}
