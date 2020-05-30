/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.gitea;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface CreateLabelOption {

    //region Builders
    static ImmutableCreateLabelOption.Builder builder() {
        return ImmutableCreateLabelOption.builder();
    }

    static CreateLabelOption of(final String name, final String color) {
        return ImmutableCreateLabelOption.of(name, color);
    }
    //endregion

    @Value.Parameter
    @JsonProperty
    String color();

    Optional<String> description();

    @Value.Parameter
    @JsonProperty
    String name();

}
