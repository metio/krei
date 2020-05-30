/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.ImmutableProject;
import wtf.metio.krei.model.Project;
import wtf.metio.krei.model.ProjectConfig;
import wtf.metio.krei.model.Unit;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Materialize {

    static Project asProject(final ProjectConfig config, final Path projectDirectory) {
        return asProject(config, MaterializeOptions.standard(projectDirectory));
    }

    static Project asProject(final ProjectConfig config, final MaterializeOptions options) {
        final var builder = Project.builder(options.projectDirectory());
        addAsUnits(config.build(), options.buildHandler(), builder);
        addAsUnits(config.license(), options.licenseHandler(), builder);
        addAsUnits(config.community(), options.communityHandler(), builder);
        addAsUnits(config.ide(), options.ideHandler(), builder);
        addAsUnits(config.vcs(), options.vcsHandler(), builder);
        return builder.build();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> void addAsUnits(
            final Optional<T> object,
            final List<Function<T, Unit>> handlers,
            final ImmutableProject.Builder builder) {
        object.map(value -> handle(value, handlers)).ifPresent(builder::addAllUnits);
    }

    private static <T> void addAsUnits(
            final List<T> objects,
            final List<Function<T, Unit>> handlers,
            final ImmutableProject.Builder builder) {
        objects.stream().map(value -> handle(value, handlers)).forEach(builder::addAllUnits);
    }

    private static <T> List<Unit> handle(final T object, final List<Function<T, Unit>> handlers) {
        return handlers.stream()
                .map(handler -> handler.apply(object))
                .collect(Collectors.toList());
    }

    private Materialize() {
        // factory class
    }

}
