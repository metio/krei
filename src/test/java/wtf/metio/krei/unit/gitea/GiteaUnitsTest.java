/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

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
