package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.given;

/**
 * Client for Stage Settings endpoints.
 * Handles stage settings operations.
 */
public class StageSettingsClient {
    private static final String BASE_PATH = "/api/v1/stages/settings";

    /**
     * Get stage settings by stage ID.
     * GET /api/v1/stages/settings/{stageId}
     * @param stageId Unique identifier of the stage
     * @return Response with stage settings
     */
    public Response getStageSettings(String stageId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .when()
                .get(BASE_PATH + "/{stageId}");
    }
}
