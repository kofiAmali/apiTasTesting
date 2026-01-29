package tests.journey;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for validating CRUD operations on Journey Tags endpoints.
 * Tests Create, Read, Update, and Delete operations for journey tags.
 */
@Epic("Journey Management")
@Feature("Journey Tags CRUD Operations")
public class JourneyTagsCRUDTest {
    private JourneyClient journeyClient;
    private CategoryClient categoryClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneyTitle;
    private String testCategoryId;
    private String testCategoryName;
    private String createdTagId;
    private String createdTagName;

    @BeforeClass
    public void setup() {
        journeyClient = new JourneyClient();
        categoryClient = new CategoryClient();
        objectMapper = new ObjectMapper();

        // Setup: Create test journey and category for tag operations
        setupTestData();
    }

    /**
     * Helper method to create test journey and category
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey and category for tag operations", () -> {
            // 1. Create test journey
            testJourneyTitle = "Journey Tags Test " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for tags testing");
            journeyRequest.put("language", "en-gb");

            Response createJourneyResponse = journeyClient.createJourney(journeyRequest);

            if (createJourneyResponse.getStatusCode() == 200 || createJourneyResponse.getStatusCode() == 201) {
                System.out.println("✓ Test journey created: " + testJourneyTitle);

                // Get the journey ID by searching
                Response getAllResponse = journeyClient.getAllJourneys(0, 10, testJourneyTitle, "", "");
                if (getAllResponse.getStatusCode() == 200) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(getAllResponse.getBody().asString());
                        JsonNode content = rootNode.get("content");
                        if (content != null && content.isArray() && content.size() > 0) {
                            testJourneyId = content.get(0).get("id").asText();
                            System.out.println("✓ Retrieved journey ID: " + testJourneyId);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract journey ID: " + e.getMessage());
                    }
                }
            }

            // 2. Create test category
            testCategoryName = "Tags Test Category " + System.currentTimeMillis();

            Map<String, Object> categoryRequest = new HashMap<>();
            categoryRequest.put("categoryName", testCategoryName);

            Response createCategoryResponse = categoryClient.createCategory(categoryRequest);

            if (createCategoryResponse.getStatusCode() == 200 || createCategoryResponse.getStatusCode() == 201) {
                System.out.println("✓ Test category created: " + testCategoryName);

                // Extract category ID from response
                try {
                    JsonNode responseNode = objectMapper.readTree(createCategoryResponse.getBody().asString());
                    if (responseNode.has("categoryId")) {
                        testCategoryId = responseNode.get("categoryId").asText();
                        System.out.println("✓ Retrieved category ID: " + testCategoryId);
                    } else if (responseNode.has("id")) {
                        testCategoryId = responseNode.get("id").asText();
                        System.out.println("✓ Retrieved category ID: " + testCategoryId);
                    }
                } catch (Exception e) {
                    System.err.println("✗ Failed to extract category ID: " + e.getMessage());
                }
            }
        });
    }

    // ===================== CREATE OPERATIONS =====================

    @Test(description = "Create journey tag - should return 201", priority = 1)
    @Story("Create Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that creating a journey tag returns 201 Created")
    public void testCreateJourneyTag_Success() {
        Allure.step("Create a new tag for journey", () -> {
            if (testJourneyId == null || testCategoryId == null) {
                System.err.println("✗ Test journey or category not available");
                return;
            }

            createdTagName = "Test Tag " + System.currentTimeMillis();

            Map<String, Object> tagRequest = new HashMap<>();
            tagRequest.put("name", createdTagName);
            tagRequest.put("categoryId", testCategoryId);

            Response response = journeyClient.createJourneyTag(testJourneyId, tagRequest);

            System.out.println("=== Create Journey Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + testJourneyId);
            System.out.println("Tag Name: " + createdTagName);
            System.out.println("Category ID: " + testCategoryId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 201 Created
            ResponseAssertions.assertStatusCode(response, 201);
            System.out.println("✓ Successfully created journey tag");

            // Get tags to extract the tag ID
            Response getTagsResponse = journeyClient.getJourneyTags(testJourneyId);
            if (getTagsResponse.getStatusCode() == 200) {
                try {
                    JsonNode tagsNode = objectMapper.readTree(getTagsResponse.getBody().asString());
                    if (tagsNode.isArray() && tagsNode.size() > 0) {
                        // Find our created tag
                        for (JsonNode tagNode : tagsNode) {
                            if (tagNode.has("tags")) {
                                JsonNode tags = tagNode.get("tags");
                                if (tags.isArray()) {
                                    for (JsonNode tag : tags) {
                                        String tagName = tag.get("name").asText();
                                        if (tagName.equals(createdTagName)) {
                                            createdTagId = tag.get("id").asText();
                                            System.out.println("✓ Retrieved created tag ID: " + createdTagId);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Failed to extract tag ID: " + e.getMessage());
                }
            }
        });
    }

    @Test(description = "Create journey tag with missing name - should return 400", priority = 2)
    @Story("Create Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a tag without name returns 400 Bad Request")
    public void testCreateJourneyTag_MissingName_Returns400() {
        Allure.step("Attempt to create tag without name", () -> {
            if (testJourneyId == null || testCategoryId == null) {
                System.err.println("✗ Test journey or category not available");
                return;
            }

            Map<String, Object> tagRequest = new HashMap<>();
            // Missing name
            tagRequest.put("categoryId", testCategoryId);

            Response response = journeyClient.createJourneyTag(testJourneyId, tagRequest);

            System.out.println("=== Create Tag Without Name Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing name");
        });
    }

    @Test(description = "Create journey tag with missing categoryId - should return 400", priority = 3)
    @Story("Create Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a tag without categoryId returns 400 Bad Request")
    public void testCreateJourneyTag_MissingCategoryId_Returns400() {
        Allure.step("Attempt to create tag without categoryId", () -> {
            if (testJourneyId == null) {
                System.err.println("✗ Test journey not available");
                return;
            }

            Map<String, Object> tagRequest = new HashMap<>();
            tagRequest.put("name", "Tag Without Category");
            // Missing categoryId

            Response response = journeyClient.createJourneyTag(testJourneyId, tagRequest);

            System.out.println("=== Create Tag Without CategoryId Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing categoryId");
        });
    }

    @Test(description = "Create journey tag with empty name - should return 400", priority = 4)
    @Story("Create Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a tag with empty name returns 400 Bad Request")
    public void testCreateJourneyTag_EmptyName_Returns400() {
        Allure.step("Attempt to create tag with empty name", () -> {
            if (testJourneyId == null || testCategoryId == null) {
                System.err.println("✗ Test journey or category not available");
                return;
            }

            Map<String, Object> tagRequest = new HashMap<>();
            tagRequest.put("name", "");  // Empty name
            tagRequest.put("categoryId", testCategoryId);

            Response response = journeyClient.createJourneyTag(testJourneyId, tagRequest);

            System.out.println("=== Create Tag With Empty Name Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for empty name (minLength: 1)");
        });
    }

    @Test(description = "Create journey tag with invalid journey ID - should return 404", priority = 5)
    @Story("Create Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a tag with invalid journey ID returns 404 Not Found")
    public void testCreateJourneyTag_InvalidJourneyId_Returns404() {
        Allure.step("Attempt to create tag with invalid journey ID", () -> {
            if (testCategoryId == null) {
                System.err.println("✗ Test category not available");
                return;
            }

            String invalidJourneyId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> tagRequest = new HashMap<>();
            tagRequest.put("name", "Tag for Invalid Journey");
            tagRequest.put("categoryId", testCategoryId);

            Response response = journeyClient.createJourneyTag(invalidJourneyId, tagRequest);

            System.out.println("=== Create Tag With Invalid Journey ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    // ===================== READ OPERATIONS =====================

    @Test(description = "Get journey tags - should return 200", priority = 6, dependsOnMethods = {"testCreateJourneyTag_Success"})
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving journey tags returns 200 OK with tag list")
    public void testGetJourneyTags_Success() {
        Allure.step("Get all tags for journey", () -> {
            if (testJourneyId == null) {
                System.err.println("✗ Test journey not available");
                return;
            }

            Response response = journeyClient.getJourneyTags(testJourneyId);

            System.out.println("=== Get Journey Tags Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + testJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify tags exist
            try {
                JsonNode tagsNode = objectMapper.readTree(response.getBody().asString());
                assert tagsNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved journey tags");
                System.out.println("Number of tag categories: " + tagsNode.size());
            } catch (Exception e) {
                System.err.println("✗ Failed to parse tags response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get journey tags with invalid journey ID - should return 404", priority = 7)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving tags with invalid journey ID returns 404 Not Found")
    public void testGetJourneyTags_InvalidJourneyId_Returns404() {
        Allure.step("Attempt to get tags with invalid journey ID", () -> {
            String invalidJourneyId = "00000000-0000-0000-0000-000000000000";

            Response response = journeyClient.getJourneyTags(invalidJourneyId);

            System.out.println("=== Get Tags With Invalid Journey ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    // ===================== UPDATE OPERATIONS =====================

    @Test(description = "Update journey tag name - should return 201", priority = 8, dependsOnMethods = {"testGetJourneyTags_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that updating a journey tag name returns 201 Created")
    public void testUpdateJourneyTag_Success() {
        Allure.step("Update journey tag name", () -> {
            if (testJourneyId == null || createdTagId == null) {
                System.err.println("✗ Test journey or tag ID not available");
                return;
            }

            String updatedTagName = createdTagName + " - Updated";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("tagId", createdTagId);
            updateRequest.put("name", updatedTagName);

            Response response = journeyClient.updateJourneyTag(testJourneyId, updateRequest);

            System.out.println("=== Update Journey Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + testJourneyId);
            System.out.println("Tag ID: " + createdTagId);
            System.out.println("New Name: " + updatedTagName);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 201 Created
            ResponseAssertions.assertStatusCode(response, 201);
            System.out.println("✓ Successfully updated journey tag");

            // Update the tag name for future tests
            createdTagName = updatedTagName;
        });
    }

    @Test(description = "Update journey tag with invalid tag ID - should return 404", priority = 9)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating with invalid tag ID returns 404 Not Found")
    public void testUpdateJourneyTag_InvalidTagId_Returns404() {
        Allure.step("Attempt to update tag with invalid tag ID", () -> {
            if (testJourneyId == null) {
                System.err.println("✗ Test journey not available");
                return;
            }

            String invalidTagId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("tagId", invalidTagId);
            updateRequest.put("name", "Updated Name");

            Response response = journeyClient.updateJourneyTag(testJourneyId, updateRequest);

            System.out.println("=== Update Tag With Invalid Tag ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Tag ID: " + invalidTagId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid tag ID");
        });
    }

    @Test(description = "Update journey tag with invalid journey ID - should return 404", priority = 10)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating with invalid journey ID returns 404 Not Found")
    public void testUpdateJourneyTag_InvalidJourneyId_Returns404() {
        Allure.step("Attempt to update tag with invalid journey ID", () -> {
            if (createdTagId == null) {
                System.err.println("✗ Tag ID not available");
                return;
            }

            String invalidJourneyId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("tagId", createdTagId);
            updateRequest.put("name", "Updated Name");

            Response response = journeyClient.updateJourneyTag(invalidJourneyId, updateRequest);

            System.out.println("=== Update Tag With Invalid Journey ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    // ===================== DELETE OPERATIONS =====================

    @Test(description = "Remove journey tag - should return 200", priority = 11, dependsOnMethods = {"testUpdateJourneyTag_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that removing a journey tag returns 200 OK")
    public void testRemoveJourneyTag_Success() {
        Allure.step("Remove tag from journey", () -> {
            if (testJourneyId == null || createdTagId == null) {
                System.err.println("✗ Test journey or tag ID not available");
                return;
            }

            Map<String, Object> removeRequest = new HashMap<>();
            List<String> tagIds = new ArrayList<>();
            tagIds.add(createdTagId);
            removeRequest.put("tagIds", tagIds);

            Response response = journeyClient.removeJourneyTag(testJourneyId, removeRequest);

            System.out.println("=== Remove Journey Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + testJourneyId);
            System.out.println("Tag ID: " + createdTagId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully removed journey tag");
        });
    }

    @Test(description = "Verify tag is removed - should not appear in tag list", priority = 12, dependsOnMethods = {"testRemoveJourneyTag_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the removed tag no longer appears in the journey tag list")
    public void testVerifyTagRemoved_Success() {
        Allure.step("Verify tag is removed from journey", () -> {
            if (testJourneyId == null || createdTagId == null) {
                System.err.println("✗ Test journey or tag ID not available");
                return;
            }

            Response response = journeyClient.getJourneyTags(testJourneyId);

            System.out.println("=== Verify Tag Removed Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify tag is not in the list
            try {
                JsonNode tagsNode = objectMapper.readTree(response.getBody().asString());
                boolean tagFound = false;

                if (tagsNode.isArray()) {
                    for (JsonNode categoryNode : tagsNode) {
                        if (categoryNode.has("tags")) {
                            JsonNode tags = categoryNode.get("tags");
                            if (tags.isArray()) {
                                for (JsonNode tag : tags) {
                                    if (tag.get("id").asText().equals(createdTagId)) {
                                        tagFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                assert !tagFound : "Tag should not be present after removal";
                System.out.println("✓ Verified tag is removed from journey");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify tag removal: " + e.getMessage());
            }
        });
    }

    @Test(description = "Remove journey tag with invalid journey ID - should return 404", priority = 13)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that removing a tag with invalid journey ID returns 404 Not Found")
    public void testRemoveJourneyTag_InvalidJourneyId_Returns404() {
        Allure.step("Attempt to remove tag with invalid journey ID", () -> {
            String invalidJourneyId = "00000000-0000-0000-0000-000000000000";
            String someTagId = "11111111-1111-1111-1111-111111111111";

            Map<String, Object> removeRequest = new HashMap<>();
            List<String> tagIds = new ArrayList<>();
            tagIds.add(someTagId);
            removeRequest.put("tagIds", tagIds);

            Response response = journeyClient.removeJourneyTag(invalidJourneyId, removeRequest);

            System.out.println("=== Remove Tag With Invalid Journey ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    @Test(description = "Remove journey tag with empty tagIds - should return 200 or 400", priority = 14)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates behavior when removing tags with empty tagIds array")
    public void testRemoveJourneyTag_EmptyTagIds() {
        Allure.step("Attempt to remove tags with empty tagIds array", () -> {
            if (testJourneyId == null) {
                System.err.println("✗ Test journey not available");
                return;
            }

            Map<String, Object> removeRequest = new HashMap<>();
            List<String> tagIds = new ArrayList<>();  // Empty list
            removeRequest.put("tagIds", tagIds);

            Response response = journeyClient.removeJourneyTag(testJourneyId, removeRequest);

            System.out.println("=== Remove Tags With Empty TagIds Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Could be 200 (no-op) or 400 (validation error)
            int statusCode = response.getStatusCode();
            assert statusCode == 200 || statusCode == 400 : "Status should be 200 or 400";
            System.out.println("✓ Returned status " + statusCode + " for empty tagIds");
        });
    }

    @AfterClass
    public void cleanup() {
        Allure.step("Cleanup: Delete test journey and category", () -> {
            // Delete test journey
            if (testJourneyId != null) {
                try {
                    Response deleteJourneyResponse = journeyClient.deleteJourney(testJourneyId);
                    if (deleteJourneyResponse.getStatusCode() == 204) {
                        System.out.println("✓ Cleanup: Test journey deleted");
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Cleanup: Failed to delete test journey: " + e.getMessage());
                }
            }

            // Delete test category
            if (testCategoryId != null) {
                try {
                    Response deleteCategoryResponse = categoryClient.deleteCategory(testCategoryId);
                    if (deleteCategoryResponse.getStatusCode() == 204 || deleteCategoryResponse.getStatusCode() == 200) {
                        System.out.println("✓ Cleanup: Test category deleted");
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Cleanup: Failed to delete test category: " + e.getMessage());
                }
            }
        });
    }
}
