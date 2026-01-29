package tests.chapter;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for validating CRUD operations on Chapter endpoints.
 * Tests Read, Update, Tags, and Delete operations.
 * Note: Create operations are tested separately in required field validation tests.
 */
@Epic("Chapter Management")
@Feature("Chapter CRUD Operations")
public class ChapterCRUDTest {
    private ChapterClient chapterClient;
    private StageClient stageClient;
    private JourneyClient journeyClient;
    private CategoryClient categoryClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneySlug;
    private String testStageId;
    private String testStageSlug;
    private String createdChapterId;
    private String createdChapterTitle;
    private String testCategoryId;
    private String createdTagId;

    @BeforeClass
    public void setup() {
        chapterClient = new ChapterClient();
        stageClient = new StageClient();
        journeyClient = new JourneyClient();
        categoryClient = new CategoryClient();
        objectMapper = new ObjectMapper();

        // Create test journey, stage, chapter, and category
        setupTestData();
    }

    /**
     * Helper method to create test journey, stage, and chapter
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey, stage, chapter, and category", () -> {
            // 1. Create test journey
            String testJourneyTitle = "Chapter CRUD Test Journey " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for chapter CRUD testing");
            journeyRequest.put("language", "en-gb");

            Response createJourneyResponse = journeyClient.createJourney(journeyRequest);

            if (createJourneyResponse.getStatusCode() == 200 || createJourneyResponse.getStatusCode() == 201) {
                System.out.println("✓ Test journey created: " + testJourneyTitle);

                // Get the journey details
                Response getAllResponse = journeyClient.getAllJourneys(0, 10, testJourneyTitle);
                if (getAllResponse.getStatusCode() == 200) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(getAllResponse.getBody().asString());
                        JsonNode content = rootNode.get("content");
                        if (content != null && content.isArray() && content.size() > 0) {
                            testJourneyId = content.get(0).get("id").asText();
                            testJourneySlug = content.get(0).get("slug").asText();
                            System.out.println("✓ Retrieved journey slug: " + testJourneySlug);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract journey details: " + e.getMessage());
                    }
                }
            }

            // 2. Create test stage
            if (testJourneySlug != null) {
                String testStageTitle = "Test Stage " + System.currentTimeMillis();

                Map<String, Object> stageRequest = new HashMap<>();
                stageRequest.put("title", testStageTitle);
                stageRequest.put("assetId", TEST_ASSET_ID);
                stageRequest.put("assetDescription", "Stage for chapter CRUD testing");
                stageRequest.put("status", "DRAFT");
                stageRequest.put("language", "en-gb");

                Response createStageResponse = stageClient.createStage(testJourneySlug, stageRequest);

                if (createStageResponse.getStatusCode() == 200 || createStageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test stage created: " + testStageTitle);

                    // Get the stage details
                    Response getStagesResponse = stageClient.getAllStages(testJourneySlug);
                    if (getStagesResponse.getStatusCode() == 200) {
                        try {
                            JsonNode stagesNode = objectMapper.readTree(getStagesResponse.getBody().asString());
                            if (stagesNode.isArray() && stagesNode.size() > 0) {
                                for (JsonNode stageNode : stagesNode) {
                                    String stageTitle = stageNode.get("content").get("title").asText();
                                    if (stageTitle.equals(testStageTitle)) {
                                        testStageId = stageNode.get("id").asText();
                                        testStageSlug = stageNode.get("slug").asText();
                                        System.out.println("✓ Retrieved stage slug: " + testStageSlug);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("✗ Failed to extract stage details: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. Create test chapter
            if (testStageSlug != null) {
                createdChapterTitle = "Test Chapter " + System.currentTimeMillis();

                Map<String, Object> chapterRequest = new HashMap<>();
                chapterRequest.put("title", createdChapterTitle);
                chapterRequest.put("assetId", TEST_ASSET_ID);
                chapterRequest.put("assetDescription", "Chapter for CRUD testing");
                chapterRequest.put("status", "DRAFT");
                chapterRequest.put("language", "en-gb");

                Response createChapterResponse = chapterClient.createChapter(testStageSlug, chapterRequest);

                if (createChapterResponse.getStatusCode() == 200 || createChapterResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test chapter created: " + createdChapterTitle);

                    // Get the chapter ID
                    if (testStageId != null) {
                        Response getChaptersResponse = chapterClient.getChaptersByStage(testStageId);
                        if (getChaptersResponse.getStatusCode() == 200) {
                            try {
                                JsonNode chaptersNode = objectMapper.readTree(getChaptersResponse.getBody().asString());
                                if (chaptersNode.isArray() && chaptersNode.size() > 0) {
                                    for (JsonNode chapterNode : chaptersNode) {
                                        String chapterTitle = chapterNode.get("content").get("title").asText();
                                        if (chapterTitle.equals(createdChapterTitle)) {
                                            createdChapterId = chapterNode.get("id").asText();
                                            System.out.println("✓ Retrieved chapter ID: " + createdChapterId);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("✗ Failed to extract chapter ID: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // 4. Create test category (for tags)
            String testCategoryName = "Chapter Tags Test Category " + System.currentTimeMillis();

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

    @Test(description = "Get chapters by stage - should return 200", priority = 1)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving all chapters in a stage returns 200 OK")
    public void testGetChaptersByStage_Success() {
        Allure.step("Get all chapters in stage", () -> {
            if (testStageId == null) {
                System.err.println("✗ Stage ID not available");
                return;
            }

            Response response = chapterClient.getChaptersByStage(testStageId);

            System.out.println("=== Get Chapters By Stage Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + testStageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify response structure
            try {
                JsonNode chaptersNode = objectMapper.readTree(response.getBody().asString());
                assert chaptersNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved chapters, count: " + chaptersNode.size());
            } catch (Exception e) {
                System.err.println("✗ Failed to parse response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get chapter by ID - should return 200", priority = 2)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving a specific chapter by ID returns 200 OK")
    public void testGetChapterById_Success() {
        Allure.step("Get chapter by ID", () -> {
            if (createdChapterId == null) {
                System.err.println("✗ Chapter ID not available");
                return;
            }

            Response response = chapterClient.getChapterById(createdChapterId);

            System.out.println("=== Get Chapter By ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + createdChapterId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify chapter details
            try {
                JsonNode chapterNode = objectMapper.readTree(response.getBody().asString());
                assert chapterNode.has("id") : "Response should contain chapter ID";
                String retrievedId = chapterNode.get("id").asText();
                assert retrievedId.equals(createdChapterId) : "Retrieved chapter ID should match";
                System.out.println("✓ Successfully retrieved chapter by ID");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify chapter details: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get chapter by invalid ID - should return 404", priority = 3)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving a chapter with invalid ID returns 404 Not Found")
    public void testGetChapterById_InvalidId_Returns404() {
        Allure.step("Attempt to get chapter with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = chapterClient.getChapterById(invalidId);

            System.out.println("=== Get Chapter By Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Chapter ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid chapter ID");
        });
    }

    // ===================== UPDATE OPERATIONS =====================

    @Test(description = "Update chapter - should return 200", priority = 4, dependsOnMethods = {"testGetChapterById_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that updating a chapter returns 200 OK")
    public void testUpdateChapter_Success() {
        Allure.step("Update chapter with new details", () -> {
            if (createdChapterId == null) {
                System.err.println("✗ Chapter ID not available for update");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", createdChapterTitle + " - Updated");
            updateRequest.put("assetId", TEST_ASSET_ID);
            updateRequest.put("assetDescription", "Updated chapter description");
            updateRequest.put("status", "DRAFT");
            updateRequest.put("language", "en-gb");

            Response response = chapterClient.updateChapter(createdChapterId, updateRequest);

            System.out.println("=== Update Chapter Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + createdChapterId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully updated chapter");
        });
    }

    @Test(description = "Update chapter with invalid ID - should return 404", priority = 5)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a chapter with invalid ID returns 404 Not Found")
    public void testUpdateChapter_InvalidId_Returns404() {
        Allure.step("Attempt to update chapter with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "Updated Chapter");
            updateRequest.put("assetId", TEST_ASSET_ID);
            updateRequest.put("status", "DRAFT");
            updateRequest.put("language", "en-gb");

            Response response = chapterClient.updateChapter(invalidId, updateRequest);

            System.out.println("=== Update Chapter With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Chapter ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid chapter ID");
        });
    }

    @Test(description = "Update chapter with missing required fields - should return 400", priority = 6)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a chapter with missing required fields returns 400 Bad Request")
    public void testUpdateChapter_MissingRequiredFields_Returns400() {
        Allure.step("Attempt to update chapter with missing required fields", () -> {
            if (createdChapterId == null) {
                System.err.println("✗ Chapter ID not available for test");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("language", "en-gb");
            // Missing title and assetId

            Response response = chapterClient.updateChapter(createdChapterId, updateRequest);

            System.out.println("=== Update Chapter With Missing Fields Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing required fields");
        });
    }

    // ===================== CHAPTER TAGS OPERATIONS =====================

    @Test(description = "Create chapter tag - should return 201", priority = 7, dependsOnMethods = {"testUpdateChapter_Success"})
    @Story("Chapter Tags Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a chapter tag returns 201 Created")
    public void testCreateChapterTag_Success() {
        Allure.step("Create a new tag for chapter", () -> {
            if (createdChapterId == null || testCategoryId == null) {
                System.err.println("✗ Chapter ID or category ID not available");
                return;
            }

            String tagName = "Chapter Test Tag " + System.currentTimeMillis();

            Map<String, Object> tagRequest = new HashMap<>();
            tagRequest.put("name", tagName);
            tagRequest.put("categoryId", testCategoryId);

            Response response = chapterClient.createChapterTag(createdChapterId, tagRequest);

            System.out.println("=== Create Chapter Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + createdChapterId);
            System.out.println("Tag Name: " + tagName);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 201 Created
            ResponseAssertions.assertStatusCode(response, 201);
            System.out.println("✓ Successfully created chapter tag");

            // Get tags to extract the tag ID
            Response getTagsResponse = chapterClient.getChapterTags(createdChapterId);
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

    @Test(description = "Get chapter tags - should return 200", priority = 8, dependsOnMethods = {"testCreateChapterTag_Success"})
    @Story("Chapter Tags Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving chapter tags returns 200 OK")
    public void testGetChapterTags_Success() {
        Allure.step("Get all tags for chapter", () -> {
            if (createdChapterId == null) {
                System.err.println("✗ Chapter ID not available");
                return;
            }

            Response response = chapterClient.getChapterTags(createdChapterId);

            System.out.println("=== Get Chapter Tags Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + createdChapterId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify tags exist
            try {
                JsonNode tagsNode = objectMapper.readTree(response.getBody().asString());
                assert tagsNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved chapter tags");
            } catch (Exception e) {
                System.err.println("✗ Failed to parse tags response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Remove chapter tag - should return 200", priority = 9, dependsOnMethods = {"testGetChapterTags_Success"})
    @Story("Chapter Tags Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that removing a chapter tag returns 200 OK")
    public void testRemoveChapterTag_Success() {
        Allure.step("Remove tag from chapter", () -> {
            if (createdChapterId == null || createdTagId == null) {
                System.err.println("✗ Chapter ID or tag ID not available");
                return;
            }

            Map<String, Object> removeRequest = new HashMap<>();
            List<String> tagIds = new ArrayList<>();
            tagIds.add(createdTagId);
            removeRequest.put("tagIds", tagIds);

            Response response = chapterClient.removeChapterTags(createdChapterId, removeRequest);

            System.out.println("=== Remove Chapter Tag Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + createdChapterId);
            System.out.println("Tag ID: " + createdTagId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully removed chapter tag");
        });
    }

    // ===================== DELETE OPERATIONS =====================

    @Test(description = "Delete chapter - should return 204", priority = 10, dependsOnMethods = {"testRemoveChapterTag_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that deleting a chapter returns 204 No Content")
    public void testDeleteChapter_Success() {
        Allure.step("Delete chapter", () -> {
            if (createdChapterId == null) {
                System.err.println("✗ Chapter ID not available for deletion");
                return;
            }

            Response response = chapterClient.deleteChapterById(createdChapterId);

            System.out.println("=== Delete Chapter Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + createdChapterId);

            // Assert 204 No Content
            ResponseAssertions.assertStatusCode(response, 204);
            System.out.println("✓ Successfully deleted chapter");
        });
    }

    @Test(description = "Verify chapter is deleted - should return 404", priority = 11, dependsOnMethods = {"testDeleteChapter_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the deleted chapter cannot be retrieved")
    public void testVerifyChapterDeleted_Returns404() {
        Allure.step("Verify chapter is deleted", () -> {
            if (createdChapterId == null) {
                System.err.println("✗ Chapter ID not available for verification");
                return;
            }

            Response response = chapterClient.getChapterById(createdChapterId);

            System.out.println("=== Verify Chapter Deleted Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Deleted Chapter ID: " + createdChapterId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Verified chapter is deleted - returns 404");
        });
    }

    @Test(description = "Delete chapter with invalid ID - should return 404", priority = 12)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that deleting with invalid ID returns 404 Not Found")
    public void testDeleteChapter_InvalidId_Returns404() {
        Allure.step("Attempt to delete chapter with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Response response = chapterClient.deleteChapterById(invalidId);

            System.out.println("=== Delete Chapter With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Chapter ID: " + invalidId);

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid chapter ID");
        });
    }

    @AfterClass
    public void cleanup() {
        Allure.step("Cleanup: Delete test journey, stage, and category", () -> {
            // Delete test journey (will cascade delete stage)
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
