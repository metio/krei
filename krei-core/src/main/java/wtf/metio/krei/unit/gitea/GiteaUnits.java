/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.gitea;

import org.immutables.value.Value;
import wtf.metio.krei.action.gitea.CreateLabelOption;
import wtf.metio.krei.action.gitea.GiteaClient;
import wtf.metio.krei.model.Unit;

@Value.Immutable
public interface GiteaUnits {

    //region Builders
    static GiteaUnits of(final GiteaClient client) {
        return ImmutableGiteaUnits.of(client);
    }
    //endregion

    //region Units
    default Unit createRepository(final String organization, final String projectName) {
        return Unit.builder()
                .id("urn:krei:gitea:repo:create")
                // .action(() -> client().createRepositoryInOrganization(projectName, organization))
                .build();
    }

    default Unit addLabel(
            final String organization,
            final String projectName,
            final CreateLabelOption label) {
        return Unit.builder()
                .id("urn:krei:gitea:label:add:" + label.name())
                // .action(() -> client().createLabel(projectName, organization, label))
                .build();
    }
    //endregion

    //region Properties
    @Value.Parameter
    GiteaClient client();
    //endregion

}
