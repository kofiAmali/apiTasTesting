package tests.pages;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for validating CRUD operations on Page endpoints.
 * Tests Read, Update, and Delete operations.
 * Note: Create operations are tested separately in required field validation tests.
 * Note: Pages do NOT have tag operations.
 */
@Epic("Page Management")
@Feature("Page CRUD Operations")
public class PageCRUDTest {
    private PageClient pageClient;
    private ChapterClient chapterClient;
    private StageClient stageClient;
    private JourneyClient journeyClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneySlug;
    private String testStageSlug;
    private String testChapterSlug;
    private String createdPageId;
    private String createdPageTitle;

    @BeforeClass
    public void setup() {
        pageClient = new PageClient();
        chapterClient = new ChapterClient();
        stageClient = new StageClient();
        journeyClient = new JourneyClient();
        objectMapper = new ObjectMapper();

        // Create test journey, stage, chapter, and page
        setupTestData();
    }

    /**
     * Helper method to create test journey, stage, chapter, and page
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey, stage, chapter, and page", () -> {
            // 1. Create test journey
            String testJourneyTitle = "Page CRUD Test Journey " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for page CRUD testing");
            journeyRequest.put("language", "en-gb");

            Response createJourneyResponse = journeyClient.createJourney(journeyRequest);

            if (createJourneyResponse.getStatusCode() == 200 || createJourneyResponse.getStatusCode() == 201) {
                System.out.println("✓ Test journey created: " + testJourneyTitle);

                // Get the journey details
                Response getAllResponse = journeyClient.getAllJourneys(0, 10, testJourneyTitle, "", "");
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
                stageRequest.put("assetDescription", "Stage for page CRUD testing");
                stageRequest.put("status", "DRAFT");
                stageRequest.put("language", "en-gb");

                Response createStageResponse = stageClient.createStage(testJourneySlug, stageRequest);

                if (createStageResponse.getStatusCode() == 200 || createStageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test stage created: " + testStageTitle);

                    // Extract stage slug directly from the create response
                    try {
                        JsonNode stageResponseNode = objectMapper.readTree(createStageResponse.getBody().asString());
                        if (stageResponseNode.has("message")) {
                            testStageSlug = stageResponseNode.get("message").asText();
                            System.out.println("✓ Retrieved stage slug: " + testStageSlug);
                        } else {
                            System.err.println("⚠ Stage created but 'message' field not found in response");
                            System.err.println("Response body: " + createStageResponse.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract stage slug: " + e.getMessage());
                    }
                }
            }

            // 3. Create test chapter
            if (testStageSlug != null) {
                String testChapterTitle = "Test Chapter " + System.currentTimeMillis();

                Map<String, Object> chapterRequest = new HashMap<>();
                chapterRequest.put("title", testChapterTitle);
                chapterRequest.put("assetId", TEST_ASSET_ID);
                chapterRequest.put("assetDescription", "Chapter for page CRUD testing");
                chapterRequest.put("status", "DRAFT");
                chapterRequest.put("language", "en-gb");

                Response createChapterResponse = chapterClient.createChapter(testStageSlug, chapterRequest);

                if (createChapterResponse.getStatusCode() == 200 || createChapterResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test chapter created: " + testChapterTitle);

                    // Extract chapter slug directly from the create response
                    try {
                        JsonNode chapterResponseNode = objectMapper.readTree(createChapterResponse.getBody().asString());
                        if (chapterResponseNode.has("message")) {
                            testChapterSlug = chapterResponseNode.get("message").asText();
                            System.out.println("✓ Retrieved chapter slug: " + testChapterSlug);
                        } else {
                            System.err.println("⚠ Chapter created but 'message' field not found in response");
                            System.err.println("Response body: " + createChapterResponse.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract chapter slug: " + e.getMessage());
                    }
                }
            }

            // 4. Create test page
            if (testChapterSlug != null) {
                createdPageTitle = "Test Page " + System.currentTimeMillis();

                Map<String, Object> pageRequest = new HashMap<>();
                pageRequest.put("templateType", "oba_image_template");
                pageRequest.put("chapterSlug", testChapterSlug);
                pageRequest.put("language", "en-gb");
                pageRequest.put("includeInPublishing", true);

                // Create page content
                Map<String, Object> content = new HashMap<>();
                content.put("templateType", "oba_image_template");
                content.put("title", createdPageTitle);
                content.put("imageId", TEST_ASSET_ID);

                // Create information content as rich text JSON
                Map<String, Object> information = new HashMap<>();
                information.put("type", "doc");
                List<Map<String, Object>> informationContent = new ArrayList<>();
                Map<String, Object> paragraph = new HashMap<>();
                paragraph.put("type", "paragraph");
                List<Map<String, Object>> paragraphContent = new ArrayList<>();
                Map<String, Object> text = new HashMap<>();
                text.put("type", "text");
                text.put("text", "Test page for CRUD operations");
                paragraphContent.add(text);
                paragraph.put("content", paragraphContent);
                informationContent.add(paragraph);
                information.put("content", informationContent);
                content.put("information", information);

                pageRequest.put("content", content);

                Response createPageResponse = pageClient.createPage(pageRequest);

                if (createPageResponse.getStatusCode() == 200 || createPageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test page created: " + createdPageTitle);

                    // Get the page ID from response or fetch pages
                    Response getPagesResponse = pageClient.getAllPagesInChapter(testChapterSlug);
                    if (getPagesResponse.getStatusCode() == 200) {
                        try {
                            JsonNode pagesNode = objectMapper.readTree(getPagesResponse.getBody().asString());
                            if (pagesNode.isArray() && pagesNode.size() > 0) {
                                for (JsonNode pageNode : pagesNode) {
                                    if (pageNode.has("content") && pageNode.get("content").has("title")) {
                                        String pageTitle = pageNode.get("content").get("title").asText();
                                        if (pageTitle.equals(createdPageTitle)) {
                                            createdPageId = pageNode.get("id").asText();
                                            System.out.println("✓ Retrieved page ID: " + createdPageId);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("✗ Failed to extract page ID: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    // ===================== READ OPERATIONS =====================

    @Test(description = "Get all pages in chapter - should return 200", priority = 1)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving all pages in a chapter returns 200 OK")
    public void testGetAllPagesInChapter_Success() {
        Allure.step("Get all pages in chapter", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Response response = pageClient.getAllPagesInChapter(testChapterSlug);

            System.out.println("=== Get All Pages In Chapter Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter Slug: " + testChapterSlug);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify response structure
            try {
                JsonNode pagesNode = objectMapper.readTree(response.getBody().asString());
                assert pagesNode.isArray() : "Response should be an array";
                System.out.println("✓ Successfully retrieved pages, count: " + pagesNode.size());
            } catch (Exception e) {
                System.err.println("✗ Failed to parse response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get page by ID - should return 200", priority = 2)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving a specific page by ID returns 200 OK")
    public void testGetPageById_Success() {
        Allure.step("Get page by ID", () -> {
            if (createdPageId == null) {
                System.err.println("✗ Page ID not available");
                return;
            }

            Response response = pageClient.getPageById(createdPageId);

            System.out.println("=== Get Page By ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Page ID: " + createdPageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify page details
            try {
                JsonNode pageNode = objectMapper.readTree(response.getBody().asString());
                assert pageNode.has("id") : "Response should contain page ID";
                String retrievedId = pageNode.get("id").asText();
                assert retrievedId.equals(createdPageId) : "Retrieved page ID should match";
                System.out.println("✓ Successfully retrieved page by ID");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify page details: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get page by invalid ID - should return 404", priority = 3)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving a page with invalid ID returns 404 Not Found")
    public void testGetPageById_InvalidId_Returns404() {
        Allure.step("Attempt to get page with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = pageClient.getPageById(invalidId);

            System.out.println("=== Get Page By Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Page ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid page ID");
        });
    }

    // ===================== UPDATE OPERATIONS =====================

    @Test(description = "Update page - should return 200", priority = 4, dependsOnMethods = {"testGetPageById_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that updating a page returns 200 OK")
    public void testUpdatePage_Success() {
        Allure.step("Update page with new details", () -> {
            if (createdPageId == null || testChapterSlug == null) {
                System.err.println("✗ Page ID or chapter slug not available for update");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("templateType", "oba_image_template");
            updateRequest.put("chapterSlug", testChapterSlug);
            updateRequest.put("language", "en-gb");
            updateRequest.put("includeInPublishing", true);

            // Create updated page content
            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_image_template");
            content.put("title", createdPageTitle + " - Updated");
            content.put("imageId", TEST_ASSET_ID);

            // Create information content as rich text JSON
            Map<String, Object> information = new HashMap<>();
            information.put("type", "doc");
            List<Map<String, Object>> informationContent = new ArrayList<>();
            Map<String, Object> paragraph = new HashMap<>();
            paragraph.put("type", "paragraph");
            List<Map<String, Object>> paragraphContent = new ArrayList<>();
            Map<String, Object> text = new HashMap<>();
            text.put("type", "text");
            text.put("text", "Updated test page content");
            paragraphContent.add(text);
            paragraph.put("content", paragraphContent);
            informationContent.add(paragraph);
            information.put("content", informationContent);
            content.put("information", information);

            updateRequest.put("content", content);

            Response response = pageClient.updatePage(createdPageId, updateRequest);

            System.out.println("=== Update Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Page ID: " + createdPageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully updated page");
        });
    }

    @Test(description = "Update page with invalid ID - should return 404", priority = 5)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a page with invalid ID returns 404 Not Found")
    public void testUpdatePage_InvalidId_Returns404() {
        Allure.step("Attempt to update page with invalid ID", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            String invalidId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("templateType", "oba_image_template");
            updateRequest.put("chapterSlug", testChapterSlug);
            updateRequest.put("language", "en-gb");

            Map<String, Object> content = new HashMap<>();
            content.put("title", "Updated Page");
            content.put("imageId", TEST_ASSET_ID);
            updateRequest.put("content", content);

            Response response = pageClient.updatePage(invalidId, updateRequest);

            System.out.println("=== Update Page With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Page ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid page ID");
        });
    }

    @Test(description = "Update page with missing required fields - should return 400", priority = 6)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a page with missing required fields returns 400 Bad Request")
    public void testUpdatePage_MissingRequiredFields_Returns400() {
        Allure.step("Attempt to update page with missing required fields", () -> {
            if (createdPageId == null) {
                System.err.println("✗ Page ID not available for test");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("language", "en-gb");
            // Missing templateType, chapterSlug, and content

            Response response = pageClient.updatePage(createdPageId, updateRequest);

            System.out.println("=== Update Page With Missing Fields Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing required fields");
        });
    }

    // ===================== DELETE OPERATIONS =====================

    @Test(description = "Delete page - should return 204", priority = 7, dependsOnMethods = {"testUpdatePage_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that deleting a page returns 204 No Content")
    public void testDeletePage_Success() {
        Allure.step("Delete page", () -> {
            if (createdPageId == null) {
                System.err.println("✗ Page ID not available for deletion");
                return;
            }

            Response response = pageClient.deletePage(createdPageId);

            System.out.println("=== Delete Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Page ID: " + createdPageId);

            // Assert 204 No Content
            ResponseAssertions.assertStatusCode(response, 204);
            System.out.println("✓ Successfully deleted page");
        });
    }

    @Test(description = "Verify page is deleted - should return 404", priority = 8, dependsOnMethods = {"testDeletePage_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the deleted page cannot be retrieved")
    public void testVerifyPageDeleted_Returns404() {
        Allure.step("Verify page is deleted", () -> {
            if (createdPageId == null) {
                System.err.println("✗ Page ID not available for verification");
                return;
            }

            Response response = pageClient.getPageById(createdPageId);

            System.out.println("=== Verify Page Deleted Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Deleted Page ID: " + createdPageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Verified page is deleted - returns 404");
        });
    }

    @Test(description = "Delete page with invalid ID - should return 404", priority = 9)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that deleting with invalid ID returns 404 Not Found")
    public void testDeletePage_InvalidId_Returns404() {
        Allure.step("Attempt to delete page with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Response response = pageClient.deletePage(invalidId);

            System.out.println("=== Delete Page With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Page ID: " + invalidId);

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid page ID");
        });
    }

    @AfterClass
    public void cleanup() {
        Allure.step("Cleanup: Delete test journey (cascades to stage and chapter)", () -> {
            // Delete test journey (will cascade delete stage and chapter)
            if (testJourneyId != null) {
                try {
                    Response deleteJourneyResponse = journeyClient.deleteJourney(testJourneyId);
                    if (deleteJourneyResponse.getStatusCode() == 204) {
                        System.out.println("✓ Cleanup: Test journey deleted (cascade deletes stage, chapter)");
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Cleanup: Failed to delete test journey: " + e.getMessage());
                }
            }
        });
    }
}
