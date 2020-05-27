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
