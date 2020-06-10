/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.validate;

import wtf.metio.krei.model.Project;
import wtf.metio.krei.model.ProjectConfig;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Validate {

    public static List<ValidationProblem> validate(final Project project) {
        throw new UnsupportedOperationException();
    }

    public static List<ValidationProblem> validate(final ProjectConfig config) {
        return validate(config, ValidationOptions.standard());
    }

    public static List<ValidationProblem> validate(final ProjectConfig config, final ValidationOptions options) {
        final var project = validate(config, options.projectConfigValidators());
        final var build = validate(config.buildSystem(), options.buildSystemValidators());
        final var license = validate(config.license(), options.licenseValidators());
        final var community = validate(config.community(), options.communityValidators());
        final var ide = validate(config.ide(), options.ideValidators());
        final var vcs = validate(config.vcs(), options.vcsValidators());
        return Stream.of(project, build, license, community, ide, vcs)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private static <T> Stream<ValidationProblem> validate(
            final Set<T> objects,
            final List<Function<T, List<ValidationProblem>>> handlers) {
        return objects.stream().flatMap(value -> validate(value, handlers));
    }

    private static <T> Stream<ValidationProblem> validate(
            final T object,
            final List<Function<T, List<ValidationProblem>>> handlers) {
        return handlers.stream()
                .flatMap(handler -> handler.apply(object).stream());
    }

    private Validate() {
        // entry point
    }

}
