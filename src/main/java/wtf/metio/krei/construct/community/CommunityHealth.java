package wtf.metio.krei.construct.community;

import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class CommunityHealth {

    public static Unit contributingGuide(final Path projectDirectory) {
        final var licenseFile = projectDirectory.resolve("CONTRIBUTING");

        return Unit.builder()
                .id("urn:krei:community:contributing")
                .build();
    }

    public static Unit codeOfConduct(final Path projectDirectory) {
        final var licenseFile = projectDirectory.resolve("CODE_OF_CONDUCT");

        return Unit.builder()
                .id("urn:krei:community:code-of-conduct")
                .build();
    }

    private CommunityHealth() {
        // factory class
    }

}
