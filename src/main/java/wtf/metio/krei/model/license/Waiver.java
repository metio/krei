/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.license;

import org.immutables.value.Value;
import wtf.metio.krei.template.LicenseTemplates;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Configuration of a license waiver.
 */
@Value.Immutable
public interface Waiver {

    //region Builders
    static ImmutableWaiver.Builder builder() {
        return ImmutableWaiver.builder().waiverFile(Paths.get("AUTHORS/WAIVER"));
    }

    static ImmutableWaiver.Builder builder(final LicenseTemplates template, final String projectName) {
        return builder().text(template.waiver(projectName));
    }

    static Waiver of(final LicenseTemplates template, final String projectName) {
        return builder()
                .text(template.waiver(projectName))
                .build();
    }
    //endregion

    /**
     * The path to the WAIVER file. Defaults to <strong>AUTHORS/WAIVER</strong>.
     */
    Optional<Path> waiverFile();

    String text();

}
