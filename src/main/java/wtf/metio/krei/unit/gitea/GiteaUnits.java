package wtf.metio.krei.unit.gitea;

import org.immutables.value.Value;
import wtf.metio.krei.gitea.GiteaClient;
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
                .action(() -> client().createRepositoryInOrganization(projectName, organization))
                .build();
    }
    //endregion

    //region Properties
    @Value.Parameter
    GiteaClient client();
    //endregion

}
