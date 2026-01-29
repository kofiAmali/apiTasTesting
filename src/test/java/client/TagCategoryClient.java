package client;

import config.*;
import io.restassured.response.*;

import static io.restassured.RestAssured.given;

/**
 * Client for Tag Category endpoints.
 * Handles tag category CRUD operations.
 */
public class TagCategoryClient {
    private static final String BASE_PATH = "/api/v1/tag-categories";

    /**
     * Get all tag categories.
     * GET /api/v1/tag-categories
     * @return Response with list of tag categories
     */
    public Response getAllTagCategories() {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .when()
                .get(BASE_PATH);
    }

    /**
     * Get tag category by ID.
     * GET /api/v1/tag-categories/{tagCategoryId}
     * @param tagCategoryId Unique identifier of the tag category
     * @return Response with tag category details
     */
    public Response getTagCategoryById(String tagCategoryId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("tagCategoryId", tagCategoryId)
                .when()
                .get(BASE_PATH + "/{tagCategoryId}");
    }

    /**
     * Create a new tag category.
     * POST /api/v1/tag-categories
     * @param requestBody Tag category creation request
     * @return Response with created tag category
     */
    public Response createTagCategory(Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .body(requestBody)
                .when()
                .post(BASE_PATH);
    }

    /**
     * Update tag category.
     * PUT /api/v1/tag-categories/{tagCategoryId}
     * @param tagCategoryId Unique identifier of the tag category
     * @param requestBody Tag category update request
     * @return Response with generic message
     */
    public Response updateTagCategory(String tagCategoryId, Object requestBody) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("tagCategoryId", tagCategoryId)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{tagCategoryId}");
    }

    /**
     * Delete tag category.
     * DELETE /api/v1/tag-categories/{tagCategoryId}
     * @param tagCategoryId Unique identifier of the tag category
     * @return Response
     */
    public Response deleteTagCategory(String tagCategoryId) {
        return given()
                .spec(RequestSpecFactory.getAdminRequestSpec())
                .pathParam("tagCategoryId", tagCategoryId)
                .when()
                .delete(BASE_PATH + "/{tagCategoryId}");
    }
}
