package info.mywinecellar.api;

import info.mywinecellar.json.MyWineCellar;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CountryITCase extends BaseITCase {

    @Test
    void countryEdit() {
        MyWineCellar response = apiRequest("/country/185/edit", jsonBody(), HttpMethod.PUT);
        assertThat(response).isNotNull();

        response.getCountries().forEach(country -> {
            assertNotNull(country.getDescription());
            assertNotNull(country.getWeblink());
        });
    }

    @Test
    void countryEdit_Exception() {
        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
                .isThrownBy(() -> apiRequest("/country/185/edit", null, HttpMethod.PUT))
                .withMessageContaining("country request for id 185 was null");
    }
}
