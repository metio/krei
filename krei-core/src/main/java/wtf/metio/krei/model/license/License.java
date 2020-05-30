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
 * Configuration of a license.
 */
@Value.Immutable
public interface License {

    //region Builders
    static ImmutableLicense.Builder builder() {
        return ImmutableLicense.builder().licenseFile(Paths.get("LICENSE"));
    }

    static ImmutableLicense.Builder builder(final LicenseTemplates template, final String projectName) {
        return builder()
                .license(template.license())
                .sourceHeader(template.header(projectName))
                .sourceHeaderMatcher(template.matcher(projectName))
                .readme(template.readme());
    }

    static License of(final LicenseTemplates template, final String projectName) {
        return builder(template, projectName)
                .waiver(Waiver.of(template, projectName))
                .build();
    }
    //endregion

    /**
     * The path to the LICENSE file. Defaults to <strong>LICENSE</strong>.
     */
    Optional<Path> licenseFile();

    String license();

    String sourceHeader();

    String sourceHeaderMatcher();

    String readme();

    Optional<Waiver> waiver();

}
