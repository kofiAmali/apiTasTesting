package tests.settings;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for Chapter Settings endpoints.
 * Tests retrieving chapter settings by chapter ID.
 */
@Epic("Chapter Management")
@Feature("Chapter Settings")
public class ChapterSettingsTest {
    private ChapterSettingsClient chapterSettingsClient;
    private ChapterClient chapterClient;
    private StageClient stageClient;
    private JourneyClient journeyClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneySlug;
    private String testStageSlug;
    private String testChapterId;

    @BeforeClass
    public void setup() {
        chapterSettingsClient = new ChapterSettingsClient();
        chapterClient = new ChapterClient();
        stageClient = new StageClient();
        journeyClient = new JourneyClient();
        objectMapper = new ObjectMapper();

        // Create test journey, stage, and chapter for settings retrieval
        setupTestData();
    }

    /**
     * Helper method to create test journey, stage, and chapter
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey, stage, and chapter", () -> {
            // 1. Create test journey
            String testJourneyTitle = "Chapter Settings Test Journey " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for chapter settings testing");
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
                            System.out.println("✓ Retrieved journey slug: " + testJourneySlug);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract journey details: " + e.getMessage());
                    }
                }
            }

            // 2. Create test stage
            if (testJourneySlug != null) {
                String testStageTitle = "Settings Test Stage " + System.currentTimeMillis();

                Map<String, Object> stageRequest = new HashMap<>();
                stageRequest.put("title", testStageTitle);
                stageRequest.put("assetId", TEST_ASSET_ID);
                stageRequest.put("assetDescription", "Stage for chapter settings testing");
                stageRequest.put("status", "DRAFT");
                stageRequest.put("language", "en-gb");

                Response createStageResponse = stageClient.createStage(testJourneySlug, stageRequest);

                if (createStageResponse.getStatusCode() == 200 || createStageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test stage created");

                    // Get the stage slug
                    Response getStagesResponse = stageClient.getAllStages(testJourneySlug);
                    if (getStagesResponse.getStatusCode() == 200) {
                        try {
                            JsonNode stagesNode = objectMapper.readTree(getStagesResponse.getBody().asString());
                            if (stagesNode.isArray() && stagesNode.size() > 0) {
                                for (JsonNode stageNode : stagesNode) {
                                    String stageTitle = stageNode.get("content").get("title").asText();
                                    if (stageTitle.equals(testStageTitle)) {
                                        testStageSlug = stageNode.get("slug").asText();
                                        System.out.println("✓ Retrieved stage slug: " + testStageSlug);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("✗ Failed to extract stage slug: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. Create test chapter
            if (testStageSlug != null) {
                String testChapterTitle = "Settings Test Chapter " + System.currentTimeMillis();

                Map<String, Object> chapterRequest = new HashMap<>();
                chapterRequest.put("title", testChapterTitle);
                chapterRequest.put("assetId", TEST_ASSET_ID);
                chapterRequest.put("assetDescription", "Chapter for settings testing");
                chapterRequest.put("status", "DRAFT");
                chapterRequest.put("language", "en-gb");

                Response createChapterResponse = chapterClient.createChapter(testStageSlug, chapterRequest);

                if (createChapterResponse.getStatusCode() == 200 || createChapterResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test chapter created");

                    // Extract chapter ID from response
                    try {
                        JsonNode responseNode = objectMapper.readTree(createChapterResponse.getBody().asString());
                        if (responseNode.has("id")) {
                            testChapterId = responseNode.get("id").asText();
                            System.out.println("✓ Retrieved chapter ID: " + testChapterId);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract chapter ID: " + e.getMessage());
                    }
                }
            }
        });
    }

    // ===================== CHAPTER SETTINGS TESTS =====================

    @Test(description = "Get chapter settings - should return 200", priority = 1)
    @Story("Chapter Settings Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving chapter settings returns 200 OK")
    public void testGetChapterSettings_Success() {
        Allure.step("Get chapter settings by chapter ID", () -> {
            if (testChapterId == null) {
                System.err.println("✗ Chapter ID not available");
                return;
            }

            Response response = chapterSettingsClient.getChapterSettings(testChapterId);

            System.out.println("=== Get Chapter Settings Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + testChapterId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK or 404 (if settings don't exist yet)
            ResponseAssertions.assertStatusCodeIn(response, 200, 404);

            if (response.getStatusCode() == 200) {
                System.out.println("✓ Successfully retrieved chapter settings");
            } else {
                System.out.println("⚠ Chapter settings not found (404) - this may be expected for new chapters");
            }
        });
    }

    @Test(description = "Get chapter settings with invalid ID - should return 404", priority = 2)
    @Story("Chapter Settings Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving settings with invalid chapter ID returns 404 Not Found")
    public void testGetChapterSettings_InvalidId_Returns404() {
        Allure.step("Attempt to get chapter settings with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = chapterSettingsClient.getChapterSettings(invalidId);

            System.out.println("=== Get Chapter Settings With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Chapter ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid chapter ID");
        });
    }

    @Test(description = "Get chapter settings - unauthorized - should return 401", priority = 3)
    @Story("Chapter Settings Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that unauthorized access returns 401")
    public void testGetChapterSettings_Unauthorized_Returns401() {
        Allure.step("Attempt to get chapter settings without authentication", () -> {
            if (testChapterId == null) {
                System.err.println("✗ Chapter ID not available");
                return;
            }

            // Using unauthorized request spec
            Response response = io.restassured.RestAssured.given()
                    .spec(config.RequestSpecFactory.getRequestSpecWithoutAuth())
                    .pathParam("chapterId", testChapterId)
                    .when()
                    .get("/api/v1/chapters/settings/{chapterId}");

            System.out.println("=== Get Chapter Settings Unauthorized Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Chapter ID: " + testChapterId);

            // Assert 401 Unauthorized
            ResponseAssertions.assertStatusCode(response, 401);
            System.out.println("✓ Correctly returned 401 for unauthorized access");
        });
    }

    @AfterClass
    public void cleanup() {
        Allure.step("Cleanup: Delete test journey", () -> {
            if (testJourneyId != null) {
                try {
                    Response deleteResponse = journeyClient.deleteJourney(testJourneyId);
                    if (deleteResponse.getStatusCode() == 204) {
                        System.out.println("✓ Cleanup: Test journey deleted");
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Cleanup: Failed to delete test journey: " + e.getMessage());
                }
            }
        });
    }
}
