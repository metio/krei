/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.metio;

import wtf.metio.krei.model.license.License;

import static wtf.metio.krei.template.LicenseTemplates.CC0;

public final class MetioLicense {

    public static License configure(final String projectName) {
        return License.of(CC0, projectName);
    }

    private MetioLicense() {
        // factory class
    }

}
