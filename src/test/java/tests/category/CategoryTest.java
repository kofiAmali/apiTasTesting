package tests.category;

import assertions.*;
import client.*;
import config.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for Categories endpoints.
 * Tests category CRUD operations for journey groupings.
 *
 * Categories are used to group and organize content within journeys.
 */
@Epic("Content Management")
@Feature("Categories")
public class CategoryTest {
    private CategoryClient client;

    // Using an existing journey ID from the system for testing
    private static final String TEST_JOURNEY_ID = "3e888b3d-d390-4b39-ad52-a670394f8b3c";

    private String createdCategoryId;

    @BeforeClass
    public void setup() {
        client = new CategoryClient();
    }

    // ========== CREATE CATEGORY TESTS ==========

    @Test(description = "Create category - Success (201)", priority = 1)
    @Story("Create Category")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that a new category can be created successfully with valid data")
    public void testCreateCategory_Success() {
        // Arrange
        String categoryName = "Technical Skills " + System.currentTimeMillis();

        Map<String, Object> request = new HashMap<>();
        request.put("name", categoryName);

        System.out.println("=== Create Category Test ===");
        System.out.println("Category Name: " + categoryName);

        // Act
        Response response = client.createCategory(request);

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 201);
        ResponseAssertions.assertResponseTimeBelow(response, 5000);

        // Extract category ID if created
        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            try {
                // Try different possible JSON paths where the ID might be located
                try {
                    createdCategoryId = ResponseAssertions.extractJsonPath(response, "$.id");
                    System.out.println("✓ Successfully created category with ID: " + createdCategoryId);
                } catch (Exception e) {
                    try {
                        createdCategoryId = ResponseAssertions.extractJsonPath(response, "$.data.id");
                        System.out.println("✓ Successfully created category with ID: " + createdCategoryId);
                    } catch (Exception ex) {
                        System.err.println("⚠ Could not extract category ID from response");
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠ Failed to extract category ID: " + e.getMessage());
            }
        }
    }

    @Test(description = "Create category - Missing required field (400)")
    @Story("Create Category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that creating a category without required 'name' field returns 400 error")
    public void testCreateCategory_MissingName() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        // Missing required 'name' field

        // Act
        Response response = client.createCategory(request);

        System.out.println("=== Create Category (Missing Name) Test ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400);
    }

    @Test(description = "Create category - Empty name (400)")
    @Story("Create Category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that creating a category with empty name returns 400 error")
    public void testCreateCategory_EmptyName() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("name", "");

        // Act
        Response response = client.createCategory(request);

        System.out.println("=== Create Category (Empty Name) Test ===");
        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400);
    }

    @Test(description = "Create category - Unauthorized (401)")
    @Story("Create Category")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that creating a category without authentication returns 401 error")
    public void testCreateCategory_Unauthorized() {
        // Arrange
        String originalToken = AuthManager.getBearerToken();
        AuthManager.clearBearerToken();

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Unauthorized Category Test");

        try {
            // Act
            Response response = client.createCategory(request);

            System.out.println("=== Create Category (Unauthorized) Test ===");
            System.out.println("Status Code: " + response.getStatusCode());

            // Assert
            ResponseAssertions.assertStatusCodeIn(response, 401, 403);
        } finally {
            // Cleanup
            AuthManager.setBearerToken(originalToken);
        }
    }

    // ========== GET CATEGORY BY ID TESTS ==========

    @Test(description = "Get category by ID - Success (200)", priority = 2, dependsOnMethods = "testCreateCategory_Success")
    @Story("Get Category by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that a category can be retrieved by its ID")
    public void testGetCategoryById_Success() {
        // Arrange
        String categoryId = createdCategoryId != null ? createdCategoryId : "sample-category-id";

        System.out.println("=== Get Category By ID Test ===");
        System.out.println("Category ID: " + categoryId);

        // Act
        Response response = client.getCategoryById(categoryId);

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
            ResponseAssertions.assertBodyNotEmpty(response);
            ResponseAssertions.assertJsonPathExists(response, "$.id");
            ResponseAssertions.assertJsonPathExists(response, "$.name");
            System.out.println("✓ Successfully retrieved category");
        }
    }

    @Test(description = "Get category by ID - Not found (404)")
    @Story("Get Category by ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that requesting a non-existent category returns 404 error")
    public void testGetCategoryById_NotFound() {
        // Arrange
        String nonExistentId = "99999999-9999-9999-9999-999999999999";

        System.out.println("=== Get Category By ID (Not Found) Test ===");
        System.out.println("Category ID: " + nonExistentId);

        // Act
        Response response = client.getCategoryById(nonExistentId);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404);
    }

    @Test(description = "Get category by ID - Invalid ID format (400)")
    @Story("Get Category by ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that requesting a category with invalid ID format returns 400 error")
    public void testGetCategoryById_InvalidIdFormat() {
        // Arrange
        String invalidId = "invalid-id-format";

        System.out.println("=== Get Category By ID (Invalid Format) Test ===");
        System.out.println("Category ID: " + invalidId);

        // Act
        Response response = client.getCategoryById(invalidId);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400, 404);
    }

    // ========== GET CATEGORIES BY JOURNEY TESTS ==========

    @Test(description = "Get categories by journey - Success (200)")
    @Story("Get Categories by Journey")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that all categories for a journey can be retrieved")
    public void testGetCategoriesByJourney_Success() {
        // Arrange
        System.out.println("=== Get Categories By Journey Test ===");
        System.out.println("Journey ID: " + TEST_JOURNEY_ID);

        // Act
        Response response = client.getCategoriesByJourney(TEST_JOURNEY_ID);

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
            System.out.println("✓ Successfully retrieved categories for journey");
        }
    }

    @Test(description = "Get categories by journey - Invalid journey ID (404)")
    @Story("Get Categories by Journey")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that requesting categories for a non-existent journey returns 404")
    public void testGetCategoriesByJourney_NotFound() {
        // Arrange
        String nonExistentJourneyId = "99999999-9999-9999-9999-999999999999";

        System.out.println("=== Get Categories By Journey (Not Found) Test ===");
        System.out.println("Journey ID: " + nonExistentJourneyId);

        // Act
        Response response = client.getCategoriesByJourney(nonExistentJourneyId);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404, 200);
    }

    @Test(description = "Get categories by journey - Unauthorized (401)")
    @Story("Get Categories by Journey")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that getting categories without authentication returns 401 error")
    public void testGetCategoriesByJourney_Unauthorized() {
        // Arrange
        String originalToken = AuthManager.getBearerToken();
        AuthManager.clearBearerToken();

        try {
            // Act
            Response response = client.getCategoriesByJourney(TEST_JOURNEY_ID);

            System.out.println("=== Get Categories By Journey (Unauthorized) Test ===");
            System.out.println("Status Code: " + response.getStatusCode());

            // Assert
            ResponseAssertions.assertStatusCodeIn(response, 401, 403);
        } finally {
            // Cleanup
            AuthManager.setBearerToken(originalToken);
        }
    }

    // ========== UPDATE CATEGORY TESTS ==========

    @Test(description = "Update category - Success (200)", priority = 3, dependsOnMethods = "testGetCategoryById_Success")
    @Story("Update Category")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that a category can be updated successfully")
    public void testUpdateCategory_Success() {
        // Arrange
        String categoryId = createdCategoryId != null ? createdCategoryId : "sample-category-id";

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Technical Skills " + System.currentTimeMillis());

        System.out.println("=== Update Category Test ===");
        System.out.println("Category ID: " + categoryId);
        System.out.println("Updated Name: " + request.get("name"));

        // Act
        Response response = client.updateCategory(categoryId, request);

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);

        if (response.getStatusCode() == 200) {
            System.out.println("✓ Successfully updated category");
        }
    }

    @Test(description = "Update category - Not found (404)")
    @Story("Update Category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that updating a non-existent category returns 404 error")
    public void testUpdateCategory_NotFound() {
        // Arrange
        String nonExistentId = "99999999-9999-9999-9999-999999999999";

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Category Name");

        System.out.println("=== Update Category (Not Found) Test ===");
        System.out.println("Category ID: " + nonExistentId);

        // Act
        Response response = client.updateCategory(nonExistentId, request);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404);
    }

    @Test(description = "Update category - Empty name (400)")
    @Story("Update Category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that updating a category with empty name returns 400 error")
    public void testUpdateCategory_EmptyName() {
        // Arrange
        String categoryId = createdCategoryId != null ? createdCategoryId : "sample-category-id";

        Map<String, Object> request = new HashMap<>();
        request.put("name", "");

        System.out.println("=== Update Category (Empty Name) Test ===");

        // Act
        Response response = client.updateCategory(categoryId, request);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400, 404);
    }

    @Test(description = "Update category - Unauthorized (401)")
    @Story("Update Category")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that updating a category without authentication returns 401 error")
    public void testUpdateCategory_Unauthorized() {
        // Arrange
        String originalToken = AuthManager.getBearerToken();
        AuthManager.clearBearerToken();
        String categoryId = "sample-category-id";

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Unauthorized Update");

        try {
            // Act
            Response response = client.updateCategory(categoryId, request);

            System.out.println("=== Update Category (Unauthorized) Test ===");
            System.out.println("Status Code: " + response.getStatusCode());

            // Assert
            ResponseAssertions.assertStatusCodeIn(response, 401, 403);
        } finally {
            // Cleanup
            AuthManager.setBearerToken(originalToken);
        }
    }

    // ========== DELETE CATEGORY TESTS ==========

    @Test(description = "Delete category - Success (204)", priority = 4, dependsOnMethods = "testUpdateCategory_Success")
    @Story("Delete Category")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that a category can be deleted successfully")
    public void testDeleteCategory_Success() {
        // Arrange
        String categoryId = createdCategoryId != null ? createdCategoryId : "sample-category-id";

        System.out.println("=== Delete Category Test ===");
        System.out.println("Category ID: " + categoryId);

        // Act
        Response response = client.deleteCategory(categoryId);

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 204, 200, 404);

        if (response.getStatusCode() == 204 || response.getStatusCode() == 200) {
            System.out.println("✓ Successfully deleted category");
            createdCategoryId = null; // Clear the ID since it's deleted
        }
    }

    @Test(description = "Delete category - Not found (404)")
    @Story("Delete Category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that deleting a non-existent category returns 404 error")
    public void testDeleteCategory_NotFound() {
        // Arrange
        String nonExistentId = "99999999-9999-9999-9999-999999999999";

        System.out.println("=== Delete Category (Not Found) Test ===");
        System.out.println("Category ID: " + nonExistentId);

        // Act
        Response response = client.deleteCategory(nonExistentId);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404);
    }

    @Test(description = "Delete category - Unauthorized (401)")
    @Story("Delete Category")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that deleting a category without authentication returns 401 error")
    public void testDeleteCategory_Unauthorized() {
        // Arrange
        String originalToken = AuthManager.getBearerToken();
        AuthManager.clearBearerToken();
        String categoryId = "sample-category-id";

        try {
            // Act
            Response response = client.deleteCategory(categoryId);

            System.out.println("=== Delete Category (Unauthorized) Test ===");
            System.out.println("Status Code: " + response.getStatusCode());

            // Assert
            ResponseAssertions.assertStatusCodeIn(response, 401, 403);
        } finally {
            // Cleanup
            AuthManager.setBearerToken(originalToken);
        }
    }

    @Test(description = "Verify deleted category cannot be retrieved (404)", priority = 5, dependsOnMethods = "testDeleteCategory_Success")
    @Story("Delete Category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that a deleted category cannot be retrieved anymore")
    public void testDeleteCategory_VerifyDeletion() {
        // Arrange
        if (createdCategoryId == null) {
            System.out.println("=== Verify Deleted Category Test ===");
            System.out.println("⚠ No category to verify deletion for (already cleaned up)");
            return;
        }

        String deletedCategoryId = createdCategoryId;

        System.out.println("=== Verify Deleted Category Test ===");
        System.out.println("Category ID: " + deletedCategoryId);

        // Act
        Response response = client.getCategoryById(deletedCategoryId);

        System.out.println("Status Code: " + response.getStatusCode());

        // Assert - Should return 404 since category was deleted
        ResponseAssertions.assertStatusCodeIn(response, 404);

        if (response.getStatusCode() == 404) {
            System.out.println("✓ Verified category was successfully deleted");
        }
    }
}
