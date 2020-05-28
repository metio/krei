/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.ide.idea;

import org.immutables.value.Value;
import wtf.metio.krei.model.ide.IDE;

import java.util.Optional;

/**
 * Configuration of Jetbrains IntelliJ IDEA
 *
 * @see <a href="https://www.jetbrains.com/idea/">jetbrains.com/idea</a>
 */
@Value.Immutable
public interface Idea extends IDE {

    //region Builders
    static ImmutableIdea.Builder builder() {
        return ImmutableIdea.builder();
    }
    //endregion

    Optional<IdeaCopyright> copyright();

    Optional<IdeaCodeStyle> codeStyle();

}
