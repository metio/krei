/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.metio;

import wtf.metio.krei.materialize.Materialize;

import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetioKrei {

    public static void main(final String[] arguments) {
        final var home = Paths.get(System.getProperty("user.home"));
        final var projectsRoot = home.resolve("git/metio/");

        final var mavenProjects = Stream.of("common-records")
                .map(MetioMavenProject::configure)
                .mapToInt(project -> Materialize.intoFilesystem(project, projectsRoot.resolve(project.name())))
                .max();

        System.exit(mavenProjects.orElse(666));
    }

}
