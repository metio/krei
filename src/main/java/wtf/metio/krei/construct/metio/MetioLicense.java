package wtf.metio.krei.construct.metio;

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
