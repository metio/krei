/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.community.readme;

import org.immutables.value.Value;
import wtf.metio.krei.model.community.Community;

import java.util.List;

@Value.Immutable
public interface Readme extends Community {

    //region Builders
    static ImmutableReadme.Builder builder() {
        return ImmutableReadme.builder();
    }

    static ImmutableReadme.Builder builder(final String title) {
        return ImmutableReadme.builder()
                .addSection(ReadmeSection.builder()
                        .titleText(title)
                        .titleLevel(1)
                        .text("TODO: Add a description of your project here")
                        .build());
    }
    //endregion

    List<ReadmeSection> section();

}
