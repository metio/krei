package wtf.metio.krei.gitea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import wtf.metio.hcf4j.HttpClient;

@Value.Immutable
public interface GiteaClient {

    //region Builders
    static ImmutableGiteaClient.Builder client(final String apiBase, final String apiKey) {
        return ImmutableGiteaClient.builder()
                .apiBase(apiBase)
                .apiKey(apiKey);
    }
    //endregion

    //region API
    default int createRepositoryInOrganization(final String projectName, final String organization) {
        final var url = String.format("%s/org/%s/repos?token=%s", apiBase(), organization, apiKey());
        try {
            final var body = objectMapper().writeValueAsString(CreateRepoOption.of(projectName));
            final var response = httpClient().post(url)
                    .content(body)
                    .mediaType("application/json")
                    .executeOnCallingThread();
            return response.statusCode();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return 1000;
        }
    }
    //endregion

    //region Properties
    String apiBase();

    String apiKey();

    HttpClient httpClient();

    ObjectMapper objectMapper();
    //endregion

}
