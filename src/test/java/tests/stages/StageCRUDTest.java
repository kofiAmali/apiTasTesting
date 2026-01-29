package tests.stages;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for validating CRUD operations on Stage endpoints.
 * Tests Read, Update, Tags, and Delete operations.
 * Note: Create operations are tested separately in required field validation tests.
 */
@Epic("Stage Management")
@Feature("Stage CRUD Operations")
public class StageCRUDTest {
    private StageClient stageClient;
    private JourneyClient journeyClient;
    private CategoryClient categoryClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneySlug;
    private String testJourneyTitle;
    private String createdStageId;
    private String createdStageTitle;
    private String testCategoryId;
    private String createdTagId;

    @BeforeClass
    public void setup() {
        stageClient = new StageClient();
        journeyClient = new JourneyClient();
        categoryClient = new CategoryClient();
        objectMapper = new ObjectMapper();

        // Create test journey, stage, and category
        setupTestData();
    }

    /**
     * Helper method to create test journey and stage
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey, stage, and category", () -> {
            // 1. Create test journey
            testJourneyTitle = "Stage CRUD Test Journey " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for stage CRUD testing");
            journeyRequest.put("language", "en-gb");

            Response createJourneyResponse = journeyClient.createJourney(journeyRequest);

            if (createJourneyResponse.getStatusCode() == 200 || createJourneyResponse.getStatusCode() == 201) {
                System.out.println("✓ Test journey created: " + testJourneyTitle);

                // Get the journey ID and slug
                Response getAllResponse = journeyClient.getAllJourneys(0, 10, testJourneyTitle, "", "");
                if (getAllResponse.getStatusCode() == 200) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(getAllResponse.getBody().asString());
                        JsonNode content = rootNode.get("content");
                        if (content != null && content.isArray() && content.size() > 0) {
                            testJourneyId = content.get(0).get("id").asText();
                            testJourneySlug = content.get(0).get("slug").asText();
                            System.out.println("✓ Retrieved journey ID: " + testJourneyId);
                            System.out.println("✓ Retrieved journey slug: " + testJourneySlug);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract journey details: " + e.getMessage());
                    }
                }
            }

            // 2. Create test stage
            if (testJourneySlug != null) {
                createdStageTitle = "Test Stage " + System.currentTimeMillis();

                Map<String, Object> stageRequest = new HashMap<>();
                stageRequest.put("title", createdStageTitle);
                stageRequest.put("assetId", TEST_ASSET_ID);
                stageRequest.put("assetDescription", "Stage for CRUD testing");
                stageRequest.put("status", "DRAFT");
                stageRequest.put("language", "en-gb");

                Response createStageResponse = stageClient.createStage(testJourneySlug, stageRequest);

                if (createStageResponse.getStatusCode() == 200 || createStageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test stage created: " + createdStageTitle);

                    // Get the stage ID by fetching all stages
                    Response getStagesResponse = stageClient.getAllStages(testJourneySlug);
                    if (getStagesResponse.getStatusCode() == 200) {
                        try {
                            JsonNode stagesNode = objectMapper.readTree(getStagesResponse.getBody().asString());
                            if (stagesNode.isArray() && stagesNode.size() > 0) {
                                // Find our created stage
                                for (JsonNode stageNode : stagesNode) {
                                    String stageTitle = stageNode.get("content").get("title").asText();
                                    if (stageTitle.equals(createdStageTitle)) {
                                        createdStageId = stageNode.get("id").asText();
                                        System.out.println("✓ Retrieved stage ID: " + createdStageId);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("✗ Failed to extract stage ID: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. Create test category (for tags)
            String testCategoryName = "Stage Tags Test Category " + System.currentTimeMillis();

            Map<String, Object> categoryRequest = new HashMap<>();
            categoryRequest.put("categoryName", testCategoryName);

            Response createCategoryResponse = categoryClient.createCategory(categoryRequest);

            if (createCategoryResponse.getStatusCode() == 200 || createCategoryResponse.getStatusCode() == 201) {
                System.out.println("✓ Test category created: " + testCategoryName);

                try {
                    JsonNode responseNode = objectMapper.readTree(createCategoryResponse.getBody().asString());
                    if (responseNode.has("categoryId")) {
                        testCategoryId = responseNode.get("categoryId").asText();
                    } else if (responseNode.has("id")) {
                        testCategoryId = responseNode.get("id").asText();
                    }
                    System.out.println("✓ Retrieved category ID: " + testCategoryId);
                } catch (Exception e) {
                    System.err.println("✗ Failed to extract category ID: " + e.getMessage());
                }
            }
        });
    }

    // ===================== READ OPERATIONS =====================

    @Test(description = "Get all stages in journey - should return 200", priority = 1)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving all stages in a journey returns 200 OK")
    public void testGetAllStages_Success() {
        Allure.step("Get all stages in journey", () -> {
            if (testJourneySlug == null) {
                System.err.println("✗ Journey slug not available");
                return;
            }

            Response response = stageClient.getAllStages(testJourneySlug);

            System.out.println("=== Get All Stages Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey Slug: " + testJourneySlug);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify response structure
            try {
                JsonNode stagesNode = objectMapper.readTree(response.getBody().asString());
                assert stagesNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved stages, count: " + stagesNode.size());
            } catch (Exception e) {
                System.err.println("✗ Failed to parse response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get stage by ID - should return 200", priority = 2)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving a specific stage by ID returns 200 OK")
    public void testGetStageById_Success() {
        Allure.step("Get stage by ID", () -> {
            if (createdStageId == null) {
                System.err.println("✗ Stage ID not available");
                return;
            }

            Response response = stageClient.getStageById(createdStageId);

            System.out.println("=== Get Stage By ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + createdStageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify stage details
            try {
                JsonNode stageNode = objectMapper.readTree(response.getBody().asString());
                assert stageNode.has("id") : "Response should contain stage ID";
                String retrievedId = stageNode.get("id").asText();
                assert retrievedId.equals(createdStageId) : "Retrieved stage ID should match";
                System.out.println("✓ Successfully retrieved stage by ID");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify stage details: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get stage by invalid ID - should return 404", priority = 3)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving a stage with invalid ID returns 404 Not Found")
    public void testGetStageById_InvalidId_Returns404() {
        Allure.step("Attempt to get stage with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = stageClient.getStageById(invalidId);

            System.out.println("=== Get Stage By Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Stage ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid stage ID");
        });
    }

    // ===================== UPDATE OPERATIONS =====================

    @Test(description = "Update stage - should return 200", priority = 4, dependsOnMethods = {"testGetStageById_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that updating a stage returns 200 OK")
    public void testUpdateStage_Success() {
        Allure.step("Update stage with new details", () -> {
            if (createdStageId == null) {
                System.err.println("✗ Stage ID not available for update");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", createdStageTitle + " - Updated");
            updateRequest.put("assetId", TEST_ASSET_ID);
            updateRequest.put("assetDescription", "Updated stage description");
            updateRequest.put("status", "DRAFT");
            updateRequest.put("language", "en-gb");

            Response response = stageClient.updateStage(createdStageId, updateRequest);

            System.out.println("=== Update Stage Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + createdStageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully updated stage");
        });
    }

    @Test(description = "Update stage with invalid ID - should return 404", priority = 5)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a stage with invalid ID returns 404 Not Found")
    public void testUpdateStage_InvalidId_Returns404() {
        Allure.step("Attempt to update stage with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "Updated Stage");
            updateRequest.put("assetId", TEST_ASSET_ID);
            updateRequest.put("status", "DRAFT");
            updateRequest.put("language", "en-gb");

            Response response = stageClient.updateStage(invalidId, updateRequest);

            System.out.println("=== Update Stage With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Stage ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid stage ID");
        });
    }

    @Test(description = "Update stage with missing required fields - should return 400", priority = 6)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a stage with missing required fields returns 400 Bad Request")
    public void testUpdateStage_MissingRequiredFields_Returns400() {
        Allure.step("Attempt to update stage with missing required fields", () -> {
            if (createdStageId == null) {
                System.err.println("✗ Stage ID not available for test");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("language", "en-gb");
            // Missing title and assetId

            Response response = stageClient.updateStage(createdStageId, updateRequest);

            System.out.println("=== Update Stage With Missing Fields Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing required fields");
        });
    }

    // ===================== STAGE TAGS OPERATIONS =====================

    @Test(description = "Create stage tag - should return 201", priority = 7, dependsOnMethods = {"testUpdateStage_Success"})
    @Story("Stage Tags Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a stage tag returns 201 Created")
    public void testCreateStageTag_Success() {
        Allure.step("Create a new tag for stage", () -> {
            if (createdStageId == null || testCategoryId == null) {
                System.err.println("✗ Stage ID or category ID not available");
                return;
            }

            String tagName = "Stage Test Tag " + System.currentTimeMillis();

            Map<String, Object> tagRequest = new HashMap<>();
            tagRequest.put("name", tagName);
            tagRequest.put("categoryId", testCategoryId);

            Response response = stageClient.createStageTag(createdStageId, tagRequest);

            System.out.println("=== Create Stage Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + createdStageId);
            System.out.println("Tag Name: " + tagName);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 201 Created
            ResponseAssertions.assertStatusCode(response, 201);
            System.out.println("✓ Successfully created stage tag");

            // Get tags to extract the tag ID
            Response getTagsResponse = stageClient.getStageTags(createdStageId);
            if (getTagsResponse.getStatusCode() == 200) {
                try {
                    JsonNode tagsNode = objectMapper.readTree(getTagsResponse.getBody().asString());
                    if (tagsNode.isArray() && tagsNode.size() > 0) {
                        for (JsonNode categoryNode : tagsNode) {
                            if (categoryNode.has("tags")) {
                                JsonNode tags = categoryNode.get("tags");
                                if (tags.isArray()) {
                                    for (JsonNode tag : tags) {
                                        if (tag.get("name").asText().equals(tagName)) {
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

    @Test(description = "Get stage tags - should return 200", priority = 8, dependsOnMethods = {"testCreateStageTag_Success"})
    @Story("Stage Tags Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving stage tags returns 200 OK")
    public void testGetStageTags_Success() {
        Allure.step("Get all tags for stage", () -> {
            if (createdStageId == null) {
                System.err.println("✗ Stage ID not available");
                return;
            }

            Response response = stageClient.getStageTags(createdStageId);

            System.out.println("=== Get Stage Tags Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + createdStageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify tags exist
            try {
                JsonNode tagsNode = objectMapper.readTree(response.getBody().asString());
                assert tagsNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved stage tags");
            } catch (Exception e) {
                System.err.println("✗ Failed to parse tags response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Remove stage tag - should return 200", priority = 9, dependsOnMethods = {"testGetStageTags_Success"})
    @Story("Stage Tags Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that removing a stage tag returns 200 OK")
    public void testRemoveStageTag_Success() {
        Allure.step("Remove tag from stage", () -> {
            if (createdStageId == null || createdTagId == null) {
                System.err.println("✗ Stage ID or tag ID not available");
                return;
            }

            Map<String, Object> removeRequest = new HashMap<>();
            List<String> tagIds = new ArrayList<>();
            tagIds.add(createdTagId);
            removeRequest.put("tagIds", tagIds);

            Response response = stageClient.removeStageTags(createdStageId, removeRequest);

            System.out.println("=== Remove Stage Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + createdStageId);
            System.out.println("Tag ID: " + createdTagId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully removed stage tag");
        });
    }

    // ===================== DELETE OPERATIONS =====================

    @Test(description = "Delete stage - should return 204", priority = 10, dependsOnMethods = {"testRemoveStageTag_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that deleting a stage returns 204 No Content")
    public void testDeleteStage_Success() {
        Allure.step("Delete stage", () -> {
            if (createdStageId == null) {
                System.err.println("✗ Stage ID not available for deletion");
                return;
            }

            Response response = stageClient.deleteStage(createdStageId);

            System.out.println("=== Delete Stage Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + createdStageId);

            // Assert 204 No Content
            ResponseAssertions.assertStatusCode(response, 204);
            System.out.println("✓ Successfully deleted stage");
        });
    }

    @Test(description = "Verify stage is deleted - should return 404", priority = 11, dependsOnMethods = {"testDeleteStage_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the deleted stage cannot be retrieved")
    public void testVerifyStageDeleted_Returns404() {
        Allure.step("Verify stage is deleted", () -> {
            if (createdStageId == null) {
                System.err.println("✗ Stage ID not available for verification");
                return;
            }

            Response response = stageClient.getStageById(createdStageId);

            System.out.println("=== Verify Stage Deleted Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Deleted Stage ID: " + createdStageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Verified stage is deleted - returns 404");
        });
    }

    @Test(description = "Delete stage with invalid ID - should return 404", priority = 12)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that deleting with invalid ID returns 404 Not Found")
    public void testDeleteStage_InvalidId_Returns404() {
        Allure.step("Attempt to delete stage with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Response response = stageClient.deleteStage(invalidId);

            System.out.println("=== Delete Stage With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Stage ID: " + invalidId);

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid stage ID");
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
