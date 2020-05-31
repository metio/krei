/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wtf.metio.krei.template.LicenseTemplates;
import wtf.metio.krei.test.FileAsserts;
import wtf.metio.krei.test.TestRunner;

import java.nio.file.Path;

@DisplayName("LicenseUnits")
class LicenseUnitsTest {

    @DisplayName("create license from text")
    @Test
    void shouldCreateLicense(@TempDir final Path projectDirectory) {
        // given
        final var licenseFile = projectDirectory.resolve("LICENSE");

        // when
        final var unit = LicenseUnits.createLicense(licenseFile, "example");

        // then
        TestRunner.assertUnit(unit);
        FileAsserts.assertFileContent(licenseFile, "example");
    }

    @DisplayName("create license from template")
    @ParameterizedTest(name = "[{index}] {0}")
    @EnumSource(LicenseTemplates.class)
    void shouldCreateLicenseFromTemplate(final LicenseTemplates template, @TempDir final Path projectDirectory) {
        // given
        final var licenseFile = projectDirectory.resolve("LICENSE");

        // when
        final var unit = LicenseUnits.createLicense(licenseFile, template);

        // then
        TestRunner.assertUnit(unit);
        FileAsserts.assertFileContent(licenseFile, template.license());
    }

    @DisplayName("create waiver from text")
    @Test
    void shouldCreateWaiver(@TempDir final Path projectDirectory) {
        // given
        final var waiverFile = projectDirectory.resolve("WAIVER");

        // when
        final var unit = LicenseUnits.createWaiver(waiverFile, "example");

        // then
        TestRunner.assertUnit(unit);
        FileAsserts.assertFileContent(waiverFile, "example");
    }

    @DisplayName("create waiver from template")
    @ParameterizedTest(name = "[{index}] {0}")
    @EnumSource(LicenseTemplates.class)
    void shouldCreateWaiverFromTemplate(final LicenseTemplates template, @TempDir final Path projectDirectory) {
        // given
        final var projectName = "test";
        final var waiverFile = projectDirectory.resolve("WAIVER");

        // when
        final var unit = LicenseUnits.createWaiver(waiverFile, template, projectName);

        // then
        TestRunner.assertUnit(unit);
        FileAsserts.assertFileContent(waiverFile, template.waiver(projectName));
    }

}
