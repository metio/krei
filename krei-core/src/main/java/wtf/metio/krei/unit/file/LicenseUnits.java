/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.file;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.task.file.FileTasks;
import wtf.metio.krei.template.LicenseTemplates;

import java.nio.file.Path;

public final class LicenseUnits {

    public static Unit createLicense(final Path path, final LicenseTemplates license) {
        return createLicense(path, license.license());
    }

    public static Unit createLicense(final Path path, final String text) {
        return Unit.builder()
                .id("urn:krei:license:file:" + path.toAbsolutePath())
                .task(FileTasks.appendFile(path, text))
                .build();
    }

    public static Unit createWaiver(final Path path, final LicenseTemplates license, final String projectName) {
        final var text = license.waiver(projectName);
        return createWaiver(path, text);
    }

    public static Unit createWaiver(final Path path, final String text) {
        return Unit.builder()
                .id("urn:krei:license:waiver:" + path.toAbsolutePath())
                .task(FileTasks.appendFile(path, text))
                .build();
    }

    public static Unit signWaiver(final Path path) {
        return Unit.builder()
                .id("urn:krei:license:waiver:sign")
                // .addExec("gpg2", "--no-version", "--armor", "--sign", path.toAbsolutePath().toString())
                .build();
    }

    public static Unit readmeSection(final Path readme, final LicenseTemplates license) {
        return FileUnits.appendFile(readme, license.readme());
    }

    private LicenseUnits() {
        // factory class
    }

}
