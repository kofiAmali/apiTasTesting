package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.*;

/**
 * Client for Chapter Management endpoints.
 * Handles chapter story management operations.
 */
public class ChapterClient {
    private static final String BASE_PATH = "/api/v1/chapters";

    /**
     * Get chapter by ID.
     * GET /api/v1/chapters/{chapterId}
     * @param chapterId Unique identifier of the chapter
     * @return Response with chapter details
     */
    public Response getChapterById(String chapterId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .when()
                .get(BASE_PATH + "/{chapterId}");
    }

    /**
     * Create a new chapter.
     * POST /api/v1/chapters
     * @param requestBody Chapter creation request
     * @return Response with created chapter
     */
    public Response createChapter(String stageSlug,Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageSlug", stageSlug)
                .body(requestBody)
                .when()
                .post(BASE_PATH + "/{stageSlug}");
    }

    /**
     * Update chapter.
     * PUT /api/v1/chapters/{chapterId}
     * @param chapterId Unique identifier of the chapter
     * @param requestBody Chapter update request
     * @return Response with generic message
     */
    public Response updateChapter(String chapterId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{chapterId}");
    }

    /**
     * Delete chapter.
     * DELETE /api/v1/chapters/{chapterId}
     * @param chapterId Unique identifier of the chapter
     * @return Response
     */
    public Response deleteChapter(String chapterId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .when()
                .delete(BASE_PATH + "/{chapterId}");
    }

    /**
     * Delete chapter by ID (from delete endpoint).
     * DELETE /api/v1/chapters/delete/{chapterId}
     * @param chapterId Unique identifier of the chapter
     * @return Response (204 No Content on success)
     */
    public Response deleteChapterById(String chapterId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .when()
                .delete(BASE_PATH + "/delete/{chapterId}");
    }

    /**
     * Get chapters for a stage.
     * GET /api/v1/chapters/stage/{stageId}
     * @param stageId Unique identifier of the stage
     * @return Response with list of chapters
     */
    public Response getChaptersByStage(String stageId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .when()
                .get(BASE_PATH + "/stage/{stageId}");
    }

    /**
     * Update chapter tags.
     * PUT /api/v1/chapters/{chapterId}/tags
     * @param chapterId Unique identifier of the chapter
     * @param requestBody Tags request
     * @return Response with generic message
     */
    public Response updateChapterTags(String chapterId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{chapterId}/tags");
    }

    /**
     * Create/Add tags to a chapter.
     * POST /api/v1/chapters/{chapterId}/tags
     * @param chapterId Unique identifier of the chapter
     * @param requestBody TagRequest with name and categoryId
     * @return Response with generic message
     */
    public Response createChapterTag(String chapterId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .body(requestBody)
                .when()
                .post(BASE_PATH + "/{chapterId}/tags");
    }

    /**
     * Get list of tags for a chapter.
     * GET /api/v1/chapters/{chapterId}/tags-list
     * @param chapterId Unique identifier of the chapter
     * @return Response with tag list
     */
    public Response getChapterTags(String chapterId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .when()
                .get(BASE_PATH + "/{chapterId}/tags-list");
    }

    /**
     * Remove chapter tags.
     * DELETE /api/v1/chapters/{chapterId}/tags
     * @param chapterId Unique identifier of the chapter
     * @param requestBody Tags request
     * @return Response with generic message
     */
    public Response removeChapterTags(String chapterId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("chapterId", chapterId)
                .body(requestBody)
                .when()
                .delete(BASE_PATH + "/{chapterId}/tags");
    }
}
