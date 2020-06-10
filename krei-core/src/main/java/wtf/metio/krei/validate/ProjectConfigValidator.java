/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.validate;

import wtf.metio.krei.model.ProjectConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ProjectConfigValidator implements Function<ProjectConfig, List<ValidationProblem>> {

    @Override
    public List<ValidationProblem> apply(final ProjectConfig config) {
        final var problems = new ArrayList<ValidationProblem>();
        if (config.license().isEmpty()) {
            problems.add(ValidationProblem.builder()
                    .severity(ProblemSeverity.ERROR)
                    .description("All open source projects need to declare their LICENSE")
                    .offendingObject(config)
                    .onlineReference("https://krei.projects.metio.wtf/errors/P00001")
                    .build());
        }
        return List.copyOf(problems);
    }

}
