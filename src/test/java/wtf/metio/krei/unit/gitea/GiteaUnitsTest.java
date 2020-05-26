package wtf.metio.krei.unit.gitea;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wtf.metio.hcf4j.okhttp3.OkHttp3HttpClientFactory;
import wtf.metio.krei.action.gitea.CodebergClient;
import wtf.metio.krei.model.Action;

class GiteaUnitsTest {

    @Test
    @Disabled
    void shouldCreateRepository() {
        final var client = CodebergClient.client("123")
                .httpClient(new OkHttp3HttpClientFactory().build())
                .objectMapper(new ObjectMapper())
                .build();
        Assertions.assertEquals(201, GiteaUnits.of(client)
                .createRepository("metio.wtf", "test")
                .action()
                .map(Action::call)
                .orElse(0));
    }

}
