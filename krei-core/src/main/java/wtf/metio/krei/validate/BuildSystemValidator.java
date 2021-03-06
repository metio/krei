/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.validate;

import wtf.metio.krei.model.build.BuildSystem;

import java.util.List;
import java.util.function.Function;

public class BuildSystemValidator implements Function<BuildSystem, List<ValidationProblem>> {

    @Override
    public List<ValidationProblem> apply(BuildSystem buildSystem) {
        return List.of();
    }

}
