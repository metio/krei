/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LicenseTemplatesTest {

    @DisplayName("source file header")
    @ParameterizedTest
    @EnumSource(LicenseTemplates.class)
    void shouldCreateHeader(final LicenseTemplates template) {
        // given
        final var projectName = "test";

        // when
        final var header = template.header(projectName);

        // then
        assertNotNull(header);
    }

    @DisplayName("license file")
    @ParameterizedTest
    @EnumSource(LicenseTemplates.class)
    void shouldCreateLicense(final LicenseTemplates template) {
        // given
        // when
        final var license = template.license();

        // then
        assertNotNull(license);
    }

    @DisplayName("source header matcher")
    @ParameterizedTest
    @EnumSource(LicenseTemplates.class)
    void shouldCreateMatcher(final LicenseTemplates template) {
        // given
        final var projectName = "test";

        // when
        final var matcher = template.matcher(projectName);

        // then
        assertNotNull(matcher);
    }

    @DisplayName("readme section")
    @ParameterizedTest
    @EnumSource(LicenseTemplates.class)
    void shouldCreateReadme(final LicenseTemplates template) {
        // given
        // when
        final var readme = template.readme();

        // then
        assertNotNull(readme);
    }

    @DisplayName("waiver file")
    @ParameterizedTest
    @EnumSource(LicenseTemplates.class)
    void shouldCreateWaiver(final LicenseTemplates template) {
        // given
        final var projectName = "test";

        // when
        final var waiver = template.waiver(projectName);

        // then
        assertNotNull(waiver);
    }

}
