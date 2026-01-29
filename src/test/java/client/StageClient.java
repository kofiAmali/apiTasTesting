package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.*;

/**
 * Client for Stages endpoints.
 * Handles stage management, updates, and reordering operations.
 */
public class StageClient {
    private static final String BASE_PATH = "/api/v1/stages";

    /**
     * Get all stages in a journey.
     * GET /api/v1/stages/{journeySlug}
     * @param journeySlug Journey slug identifier
     * @return Response with list of stages
     */
    public Response getAllStages(String journeySlug) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeySlug", journeySlug)
                .when()
                .get(BASE_PATH + "/{journeySlug}");
    }

    /**
     * Get stage by ID.
     * GET /api/v1/stages/{stageId}
     * @param stageId Unique identifier of the stage
     * @return Response with stage details
     */
    public Response getStageById(String stageId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .when()
                .get(BASE_PATH + "/{stageId}");
    }

    /**
     * Create a new stage.
     * POST /api/v1/stages/{journeySlug}
     * @param journeySlug Journey slug identifier
     * @param requestBody StageChapterRequest object
     * @return Response with generic message
     */
    public Response createStage(String journeySlug, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeySlug", journeySlug)
                .body(requestBody)
                .when()
                .post(BASE_PATH + "/{journeySlug}");
    }

    /**
     * Update a stage.
     * PUT /api/v1/stages/{stageId}
     * @param stageId Unique identifier of the stage
     * @param requestBody StageChapterRequest object
     * @return Response with generic message
     */
    public Response updateStage(String stageId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{stageId}");
    }

    /**
     * Reorder stages in a journey.
     * PUT /api/v1/stages/{journeySlug}/reorder
     * @param journeySlug Journey slug identifier
     * @param requestBody StoryReOrderRequest with list of stage IDs in desired order
     * @return Response with generic message
     */
    public Response reorderStages(String journeySlug, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeySlug", journeySlug)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{journeySlug}/reorder");
    }

    /**
     * Update stage tags.
     * PUT /api/v1/stages/{stageId}/tags
     * @param stageId Unique identifier of the stage
     * @param requestBody StoryTagRequest with tag IDs
     * @return Response with generic message
     */
    public Response updateStageTags(String stageId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{stageId}/tags");
    }

    /**
     * Create/Add tags to a stage.
     * POST /api/v1/stages/{stageId}/tags
     * @param stageId Unique identifier of the stage
     * @param requestBody TagRequest with name and categoryId
     * @return Response with generic message
     */
    public Response createStageTag(String stageId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .body(requestBody)
                .when()
                .post(BASE_PATH + "/{stageId}/tags");
    }

    /**
     * Get list of tags for a stage.
     * GET /api/v1/stages/{stageId}/tags-list
     * @param stageId Unique identifier of the stage
     * @return Response with tag list
     */
    public Response getStageTags(String stageId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .when()
                .get(BASE_PATH + "/{stageId}/tags-list");
    }

    /**
     * Remove stage tags.
     * DELETE /api/v1/stages/{stageId}/tags
     * @param stageId Unique identifier of the stage
     * @param requestBody StoryTagRequest with tag IDs to remove
     * @return Response with generic message
     */
    public Response removeStageTags(String stageId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .body(requestBody)
                .when()
                .delete(BASE_PATH + "/{stageId}/tags");
    }

    /**
     * Delete a stage.
     * DELETE /api/v1/stages/delete/{stageId}
     * @param stageId Unique identifier of the stage
     * @return Response
     */
    public Response deleteStage(String stageId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("stageId", stageId)
                .when()
                .delete(BASE_PATH + "/delete/{stageId}");
    }
}
