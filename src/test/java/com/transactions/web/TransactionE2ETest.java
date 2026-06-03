package com.transactions.web;

import com.transactions.web.dto.SumResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests E2E reales: levantan el servidor embebido en un puerto aleatorio y le
 * pegan por HTTP con un cliente real (serialización JSON + socket de verdad),
 * a diferencia de los de integración con MockMvc. Cima fina de la pirámide.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionE2ETest {

    @LocalServerPort
    private int port;

    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    private int putTransaction(RestClient client, long id, String body) {
        return client.put()
                .uri("/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange((request, response) -> response.getStatusCode().value());
    }

    @Test
    void replicatesPdfFlowOverRealHttp() {
        RestClient client = client();

        assertThat(putTransaction(client, 10L, "{\"amount\":5000,\"type\":\"cars\"}")).isEqualTo(200);
        assertThat(putTransaction(client, 11L, "{\"amount\":10000,\"type\":\"shopping\",\"parent_id\":10}")).isEqualTo(200);
        assertThat(putTransaction(client, 12L, "{\"amount\":5000,\"type\":\"shopping\",\"parent_id\":11}")).isEqualTo(200);

        Long[] cars = client.get().uri("/transactions/types/cars").retrieve().body(Long[].class);
        assertThat(cars).containsExactly(10L);

        SumResponse sum10 = client.get().uri("/transactions/sum/10").retrieve().body(SumResponse.class);
        assertThat(sum10.sum()).isEqualTo(20000d);

        SumResponse sum11 = client.get().uri("/transactions/sum/11").retrieve().body(SumResponse.class);
        assertThat(sum11.sum()).isEqualTo(15000d);
    }

    @Test
    void parentNotFoundReturns422OverRealHttp() {
        int status = putTransaction(client(), 90L, "{\"amount\":1,\"type\":\"x\",\"parent_id\":999}");
        assertThat(status).isEqualTo(422);
    }
}
