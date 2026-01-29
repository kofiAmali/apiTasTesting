package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.*;

/**
 * Client for Journey endpoints.
 * Handles journey retrieval, updates, and management operations.
 */
public class JourneyClient {
    private static final String BASE_PATH = "/api/v1";

    /**
     * Get all journeys.
     * GET /api/v1/journeys
     * @return Response with paginated list of journeys
     */
    public Response getAllJourneys() {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .when()
                .get(BASE_PATH + "/journeys");
    }

    public Response getAllJourneys(int page, int size, String search) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("search", search)
                .when()
                .get(BASE_PATH + "/journeys");
    }

    /**
     * Get all journeys with filters.
     * GET /api/v1/journeys
     * @param page Page number
     * @param size Page size
     * @param search Search term
     * @param status Status filter
     * @param slug Slug filter
     * @return Response with paginated list of journeys
     */
    public Response getAllJourneys(int page, int size, String search, String status, String slug) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("search", search)
                .queryParam("status", status)
                .queryParam("slug", slug)
                .when()
                .get(BASE_PATH + "/journeys");
    }

    /**
     * Create a new journey.
     * POST /api/v1/journeys
     * @param requestBody Journey creation request
     * @return Response with generic message containing journey slug
     */
    public Response createJourney(Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .body(requestBody)
                .when()
                .post(BASE_PATH + "/journeys");
    }

    /**
     * Get journey by ID.
     * GET /api/v1/journey/{journeyId}
     * @param journeyId Unique identifier of the journey
     * @return Response with journey details
     */
    public Response getJourneyById(String journeyId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .when()
                .get(BASE_PATH + "/journey/{journeyId}");
    }

    /**
     * Update journey.
     * PUT /api/v1/journey/{journeyId}
     * @param journeyId Unique identifier of the journey
     * @param requestBody Journey update request object
     * @return Response with generic message
     */
    public Response updateJourney(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/journey/{journeyId}");
    }

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
                .get(BASE_PATH + "/journey/{journeyId}/settings");
    }

    /**
     * Generate preview token for journey.
     * GET /api/v1/preview/{journeyId}
     * @param journeyId Unique identifier of the journey
     * @return Response with preview token
     */
    public Response generatePreviewToken(String journeyId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .when()
                .get(BASE_PATH + "/preview/{journeyId}");
    }

    /**
     * Archive a journey.
     * PATCH /api/v1/journeys/archive
     * @param requestBody Archive request with journeyId and status
     * @return Response with generic message
     */
    public Response archiveJourney(Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .body(requestBody)
                .when()
                .patch(BASE_PATH + "/journeys/archive");
    }

    /**
     * Delete a journey by ID.
     * DELETE /api/v1/journeys/delete/{journeyId}
     * @param journeyId Unique identifier of the journey to delete
     * @return Response (204 No Content on success)
     */
    public Response deleteJourney(String journeyId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .when()
                .delete(BASE_PATH + "/journeys/delete/{journeyId}");
    }

    // ===================== JOURNEY TAGS OPERATIONS =====================

    /**
     * Create/Add a tag to a journey.
     * POST /api/v1/journeys/{journeyId}/tags
     * @param journeyId Unique identifier of the journey
     * @param requestBody TagRequest with name and categoryId
     * @return Response with generic message (201 Created)
     */
    public Response createJourneyTag(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .post(BASE_PATH + "/journeys/{journeyId}/tags");
    }

    /**
     * Get list of tags for a journey.
     * GET /api/v1/journeys/{journeyId}/tags-list
     * @param journeyId Unique identifier of the journey
     * @return Response with tag list
     */
    public Response getJourneyTags(String journeyId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .when()
                .get(BASE_PATH + "/journeys/{journeyId}/tags-list");
    }

    /**
     * Update a journey tag name.
     * PATCH /api/v1/journeys/{journeyId}/tags
     * @param journeyId Unique identifier of the journey
     * @param requestBody TagUpdateRequest with tagId and new name
     * @return Response with generic message (201 Created)
     */
    public Response updateJourneyTag(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .patch(BASE_PATH + "/journeys/{journeyId}/tags");
    }

    /**
     * Remove tag(s) from a journey.
     * DELETE /api/v1/journeys/{journeyId}/tags
     * @param journeyId Unique identifier of the journey
     * @param requestBody StoryTagRequest with tagIds array
     * @return Response with generic message (200 OK)
     */
    public Response removeJourneyTag(String journeyId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("journeyId", journeyId)
                .body(requestBody)
                .when()
                .delete(BASE_PATH + "/journeys/{journeyId}/tags");
    }
}
