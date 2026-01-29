package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.*;

/**
 * Client for Language endpoints.
 * Handles supported language operations.
 */
public class LanguageClient {
    private static final String BASE_PATH = "/api/v1/languages";

    /**
     * Get all supported languages.
     * GET /api/v1/languages
     * @return Response with list of languages
     */
    public Response getAllLanguages() {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .when()
                .get(BASE_PATH);
    }

    /**
     * Get language by code.
     * GET /api/v1/languages/{languageCode}
     * @param languageCode Language code (e.g., en, de, fr)
     * @return Response with language details
     */
    public Response getLanguageByCode(String languageCode) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("languageCode", languageCode)
                .when()
                .get(BASE_PATH + "/{languageCode}");
    }
}
