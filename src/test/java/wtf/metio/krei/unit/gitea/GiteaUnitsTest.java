package wtf.metio.krei.unit.gitea;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wtf.metio.hcf4j.okhttp3.OkHttp3HttpClientFactory;
import wtf.metio.krei.gitea.GiteaClient;
import wtf.metio.krei.model.Action;

class GiteaUnitsTest {

    @Test
    @Disabled
    void shouldCreateRepository() {
        final var giteaClient = GiteaClient.client("https://codeberg.org/api/v1",
                "123")
                .httpClient(new OkHttp3HttpClientFactory().build())
                .objectMapper(new ObjectMapper())
                .build();
        final var gitea = GiteaUnits.of(giteaClient);
        Assertions.assertEquals(201, gitea
                .createRepository("metio.wtf", "krei")
                .action()
                .map(Action::call)
                .orElse(0));
    }

}
