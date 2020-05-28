/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.vcs.git;

import org.immutables.value.Value;
import wtf.metio.krei.model.vcs.VCS;

import java.util.List;
import java.util.Optional;

/**
 * Configuration of a git repository.
 */
@Value.Immutable
public interface Git extends VCS {

    //region Builders
    static ImmutableGit.Builder builder() {
        return ImmutableGit.builder();
    }
    //endregion

    Optional<GitIgnore> ignore();

    Optional<GitConfig> config();

    List<GitRemote> remotes();

}
