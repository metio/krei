/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.license.Waiver;
import wtf.metio.krei.unit.file.FileUnits;
import wtf.metio.krei.unit.file.LicenseUnits;

import java.nio.file.Path;
import java.util.function.Function;

public final class LicenseHandler implements Function<License, Unit> {

    private final Path projectDirectory;

    LicenseHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final License license) {
        final var licenseFile = license.licenseFile()
                .map(projectDirectory::resolve)
                .orElse(projectDirectory.resolve("LICENSE"));
        final var builder = Unit.builder()
                .id("urn:krei:materialize:license")
                .addRequires(LicenseUnits.createLicense(licenseFile, license.license()));
        license.waiver().map(this::handleWaiver).ifPresent(builder::addRequires);
        return builder.build();
    }

    private Unit handleWaiver(final Waiver waiver) {
        final var waiverFile = waiver.waiverFile()
                .map(projectDirectory::resolve)
                .orElse(projectDirectory.resolve("AUTHORS/WAIVER"));
        return Unit.builder()
                .id("urn:krei:materialize:waiver")
                .addBefore(FileUnits.createDirectory(waiverFile.getParent()))
                .addRequires(LicenseUnits.createWaiver(waiverFile, waiver.text()))
                // TODO: enable waiver signature
                // .addWants(LicenseUnits.signWaiver(waiverFile))
                .build();
    }

}
