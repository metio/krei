package wtf.metio.krei.gitea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import wtf.metio.hcf4j.HttpClient;

/**
 * @see <a href="https://codeberg.org/api/swagger#">OpenAPI documentation</a>
 */
@Value.Immutable
public interface CodebergClient extends GiteaClient {

    //region Builders
    static ImmutableCodebergClient.Builder client(final String apiKey) {
        return client("https://codeberg.org/api/v1", apiKey);
    }

    static ImmutableCodebergClient.Builder client(final String apiBase, final String apiKey) {
        return ImmutableCodebergClient.builder()
                .apiBase(apiBase)
                .apiKey(apiKey);
    }
    //endregion

    //region API
    default int createRepositoryInOrganization(final String projectName, final String organization) {
        final var url = String.format("%s/org/%s/repos", apiBase(), organization);
        return post(CreateRepoOption.of(projectName), url);
    }

    default int createLabel(final String projectName, final String organization, final CreateLabelOption label) {
        final var url = String.format("%s/repos/%s/%s/labels", apiBase(), organization, projectName);
        return post(label, url);
    }
    //endregion

    //region Utils
    private int post(final Object data, final String url) {
        try {
            final var body = objectMapper().writeValueAsString(data);
            final var response = httpClient().post(url)
                    .header("Authorization", "token " + apiKey())
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
