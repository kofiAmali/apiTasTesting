package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.*;

/**
 * Client for Journey Settings endpoints.
 * Handles journey configuration, welcome messages, and language settings.
 */
public class JourneySettingsClient {
    private static final String BASE_PATH = "/api/v1/journey";

    /**
     * Get journey settings.
     * GET /api/v1/journey/{journeyId}/settings
     * @param journeyId Unique identifier of the journey
     * @return Response with journey settings
     */
    public Response getJourneySettings(String journeyId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .when()
                .get(BASE_PATH + "/{journeyId}/settings");
    }

    /**
     * Update journey settings.
     * PUT /api/v1/journey/{journeyId}/settings
     * @param journeyId Unique identifier of the journey
     * @param requestBody Journey settings update request
     * @return Response with generic message
     */
    public Response updateJourneySettings(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getRequestSpecWithoutAuth())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{journeyId}/settings");
    }

    /**
     * Update journey welcome message.
     * PUT /api/v1/journey/{journeyId}/welcome-message
     * @param journeyId Unique identifier of the journey
     * @param requestBody Welcome message request
     * @return Response with generic message
     */
    public Response updateWelcomeMessage(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{journeyId}/welcome-message");
    }

    /**
     * Update journey language settings.
     * PUT /api/v1/journey/{journeyId}/language
     * @param journeyId Unique identifier of the journey
     * @param requestBody Language settings request
     * @return Response with generic message
     */
    public Response updateLanguageSettings(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{journeyId}/language");
    }
}
