package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.given;

/**
 * Client for Chapter Settings endpoints.
 * Handles chapter settings operations.
 */
public class ChapterSettingsClient {
    private static final String BASE_PATH = "/api/v1/chapters/settings";

    /**
     * Get chapter settings by chapter ID.
     * GET /api/v1/chapters/settings/{chapterId}
     * @param chapterId Unique identifier of the chapter
     * @return Response with chapter settings
     */
    public Response getChapterSettings(String chapterId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .when()
                .get(BASE_PATH + "/{chapterId}");
    }
}
