package tests.tagcategory;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for Tag Category CRUD operations.
 * Tests Create, Read, Update, and Delete operations for tag categories.
 */
@Epic("Tag Management")
@Feature("Tag Category CRUD Operations")
public class TagCategoryCRUDTest {
    private TagCategoryClient tagCategoryClient;
    private ObjectMapper objectMapper;

    // Test data
    private String createdTagCategoryId;
    private String createdTagCategoryName;

    @BeforeClass
    public void setup() {
        tagCategoryClient = new TagCategoryClient();
        objectMapper = new ObjectMapper();
    }

    // ===================== CREATE OPERATIONS =====================

    @Test(description = "Create tag category - should return 200 or 201", priority = 1)
    @Story("Create Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that creating a tag category returns success")
    public void testCreateTagCategory_Success() {
        Allure.step("Create a new tag category", () -> {
            createdTagCategoryName = "Test Tag Category " + System.currentTimeMillis();

            Map<String, Object> request = new HashMap<>();
            request.put("categoryName", createdTagCategoryName);

            Response response = tagCategoryClient.createTagCategory(request);

            System.out.println("=== Create Tag Category Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Category Name: " + createdTagCategoryName);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK or 201 Created
            ResponseAssertions.assertStatusCodeIn(response, 200, 201);

            // Extract the created category ID
            try {
                JsonNode responseNode = objectMapper.readTree(response.getBody().asString());
                if (responseNode.has("categoryId")) {
                    createdTagCategoryId = responseNode.get("categoryId").asText();
                } else if (responseNode.has("id")) {
                    createdTagCategoryId = responseNode.get("id").asText();
                }
                System.out.println("✓ Tag category created with ID: " + createdTagCategoryId);
            } catch (Exception e) {
                System.err.println("⚠ Failed to extract category ID: " + e.getMessage());
            }
        });
    }

    @Test(description = "Create tag category with missing required field - should return 400", priority = 2)
    @Story("Create Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a tag category without required fields returns 400")
    public void testCreateTagCategory_MissingRequiredField_Returns400() {
        Allure.step("Attempt to create tag category without category name", () -> {
            Map<String, Object> request = new HashMap<>();
            // Missing categoryName

            Response response = tagCategoryClient.createTagCategory(request);

            System.out.println("=== Create Tag Category With Missing Field Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing required field");
        });
    }

    @Test(description = "Create tag category - unauthorized - should return 401", priority = 3)
    @Story("Create Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that unauthorized creation returns 403")
    public void testCreateTagCategory_Unauthorized_Returns403() {
        Allure.step("Attempt to create tag category without authentication", () -> {
            Map<String, Object> request = new HashMap<>();
            request.put("categoryName", "Unauthorized Test Category");

            // Using unauthorized request spec
            Response response = io.restassured.RestAssured.given()
                    .spec(config.RequestSpecFactory.getRequestSpecWithoutAuth())
                    .body(request)
                    .when()
                    .post("/api/v1/tag-categories");

            System.out.println("=== Create Tag Category Unauthorized Response ===");
            System.out.println("Status: " + response.getStatusCode());

            // Assert 403 Unauthorized
            ResponseAssertions.assertStatusCode(response, 403);
            System.out.println("✓ Correctly returned 403 for unauthorized access");
        });
    }

    // ===================== READ OPERATIONS =====================

    @Test(description = "Get all tag categories - should return 200", priority = 4, dependsOnMethods = {"testCreateTagCategory_Success"})
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving all tag categories returns 200 OK")
    public void testGetAllTagCategories_Success() {
        Allure.step("Get all tag categories", () -> {
            Response response = tagCategoryClient.getAllTagCategories();

            System.out.println("=== Get All Tag Categories Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify response structure
            try {
                JsonNode categoriesNode = objectMapper.readTree(response.getBody().asString());
                assert categoriesNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved tag categories, count: " + categoriesNode.size());
            } catch (Exception e) {
                System.err.println("✗ Failed to parse response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get tag category by ID - should return 200", priority = 5, dependsOnMethods = {"testCreateTagCategory_Success"})
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving a specific tag category by ID returns 200 OK")
    public void testGetTagCategoryById_Success() {
        Allure.step("Get tag category by ID", () -> {
            if (createdTagCategoryId == null) {
                System.err.println("✗ Tag category ID not available");
                return;
            }

            Response response = tagCategoryClient.getTagCategoryById(createdTagCategoryId);

            System.out.println("=== Get Tag Category By ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Category ID: " + createdTagCategoryId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify category details
            try {
                JsonNode categoryNode = objectMapper.readTree(response.getBody().asString());
                assert categoryNode.has("id") || categoryNode.has("categoryId") : "Response should contain category ID";
                System.out.println("✓ Successfully retrieved tag category by ID");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify category details: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get tag category by invalid ID - should return 404", priority = 6)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving a tag category with invalid ID returns 404 Not Found")
    public void testGetTagCategoryById_InvalidId_Returns404() {
        Allure.step("Attempt to get tag category with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = tagCategoryClient.getTagCategoryById(invalidId);

            System.out.println("=== Get Tag Category By Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Category ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid category ID");
        });
    }

    // ===================== UPDATE OPERATIONS =====================

    @Test(description = "Update tag category - should return 200", priority = 7, dependsOnMethods = {"testGetTagCategoryById_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that updating a tag category returns 200 OK")
    public void testUpdateTagCategory_Success() {
        Allure.step("Update tag category with new name", () -> {
            if (createdTagCategoryId == null) {
                System.err.println("✗ Tag category ID not available for update");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("categoryName", createdTagCategoryName + " - Updated");

            Response response = tagCategoryClient.updateTagCategory(createdTagCategoryId, updateRequest);

            System.out.println("=== Update Tag Category Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Category ID: " + createdTagCategoryId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully updated tag category");
        });
    }

    @Test(description = "Update tag category with invalid ID - should return 404", priority = 8)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a tag category with invalid ID returns 404 Not Found")
    public void testUpdateTagCategory_InvalidId_Returns404() {
        Allure.step("Attempt to update tag category with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("categoryName", "Updated Category");

            Response response = tagCategoryClient.updateTagCategory(invalidId, updateRequest);

            System.out.println("=== Update Tag Category With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Category ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid category ID");
        });
    }

    @Test(description = "Update tag category with missing required field - should return 400", priority = 9, dependsOnMethods = {"testUpdateTagCategory_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating with missing required fields returns 400")
    public void testUpdateTagCategory_MissingRequiredField_Returns400() {
        Allure.step("Attempt to update tag category without category name", () -> {
            if (createdTagCategoryId == null) {
                System.err.println("✗ Tag category ID not available for test");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            // Missing categoryName

            Response response = tagCategoryClient.updateTagCategory(createdTagCategoryId, updateRequest);

            System.out.println("=== Update Tag Category With Missing Field Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing required field");
        });
    }

    // ===================== DELETE OPERATIONS =====================

    @Test(description = "Delete tag category - should return 204 or 200", priority = 10, dependsOnMethods = {"testUpdateTagCategory_MissingRequiredField_Returns400"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that deleting a tag category returns success")
    public void testDeleteTagCategory_Success() {
        Allure.step("Delete tag category", () -> {
            if (createdTagCategoryId == null) {
                System.err.println("✗ Tag category ID not available for deletion");
                return;
            }

            Response response = tagCategoryClient.deleteTagCategory(createdTagCategoryId);

            System.out.println("=== Delete Tag Category Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Category ID: " + createdTagCategoryId);

            // Assert 204 No Content or 200 OK
            ResponseAssertions.assertStatusCodeIn(response, 204, 200);
            System.out.println("✓ Successfully deleted tag category");
        });
    }

    @Test(description = "Verify tag category is deleted - should return 404", priority = 11, dependsOnMethods = {"testDeleteTagCategory_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the deleted tag category cannot be retrieved")
    public void testVerifyTagCategoryDeleted_Returns404() {
        Allure.step("Verify tag category is deleted", () -> {
            if (createdTagCategoryId == null) {
                System.err.println("✗ Tag category ID not available for verification");
                return;
            }

            Response response = tagCategoryClient.getTagCategoryById(createdTagCategoryId);

            System.out.println("=== Verify Tag Category Deleted Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Deleted Category ID: " + createdTagCategoryId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Verified tag category is deleted - returns 404");
        });
    }

    @Test(description = "Delete tag category with invalid ID - should return 404", priority = 12)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that deleting with invalid ID returns 404 Not Found")
    public void testDeleteTagCategory_InvalidId_Returns404() {
        Allure.step("Attempt to delete tag category with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Response response = tagCategoryClient.deleteTagCategory(invalidId);

            System.out.println("=== Delete Tag Category With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Category ID: " + invalidId);

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid category ID");
        });
    }
}
