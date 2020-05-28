/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.community.readme.Readme;
import wtf.metio.krei.model.community.readme.ReadmeSection;

public final class MetioReadme {

    public static Community configure(final String projectName) {
        return Readme.builder(projectName)
                .addSection(ReadmeSection.of("Usage", 2))
                .addSection(ReadmeSection.of("Alternatives", 2))
                .addSection(ReadmeSection.of("License", 2))
                .addSection(ReadmeSection.of("Mirrors", 2))
                .build();
    }

    private MetioReadme() {
        // factory class
    }

}
