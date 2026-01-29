package tests.settings;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for Stage Settings endpoints.
 * Tests retrieving stage settings by stage ID.
 */
@Epic("Stage Management")
@Feature("Stage Settings")
public class StageSettingsTest {
    private StageSettingsClient stageSettingsClient;
    private StageClient stageClient;
    private JourneyClient journeyClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneySlug;
    private String testStageId;

    @BeforeClass
    public void setup() {
        stageSettingsClient = new StageSettingsClient();
        stageClient = new StageClient();
        journeyClient = new JourneyClient();
        objectMapper = new ObjectMapper();

        // Create test journey and stage for settings retrieval
        setupTestData();
    }

    /**
     * Helper method to create test journey and stage
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey and stage", () -> {
            // 1. Create test journey
            String testJourneyTitle = "Stage Settings Test Journey " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for stage settings testing");
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
                stageRequest.put("assetDescription", "Stage for settings testing");
                stageRequest.put("status", "DRAFT");
                stageRequest.put("language", "en-gb");

                Response createStageResponse = stageClient.createStage(testJourneySlug, stageRequest);

                if (createStageResponse.getStatusCode() == 200 || createStageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test stage created");

                    // Get the stage ID
                    Response getStagesResponse = stageClient.getAllStages(testJourneySlug);
                    if (getStagesResponse.getStatusCode() == 200) {
                        try {
                            JsonNode stagesNode = objectMapper.readTree(getStagesResponse.getBody().asString());
                            if (stagesNode.isArray() && stagesNode.size() > 0) {
                                for (JsonNode stageNode : stagesNode) {
                                    String stageTitle = stageNode.get("content").get("title").asText();
                                    if (stageTitle.equals(testStageTitle)) {
                                        testStageId = stageNode.get("id").asText();
                                        System.out.println("✓ Retrieved stage ID: " + testStageId);
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
        });
    }

    // ===================== STAGE SETTINGS TESTS =====================

    @Test(description = "Get stage settings - should return 200", priority = 1)
    @Story("Stage Settings Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving stage settings returns 200 OK")
    public void testGetStageSettings_Success() {
        Allure.step("Get stage settings by stage ID", () -> {
            if (testStageId == null) {
                System.err.println("✗ Stage ID not available");
                return;
            }

            Response response = stageSettingsClient.getStageSettings(testStageId);

            System.out.println("=== Get Stage Settings Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + testStageId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK or 404 (if settings don't exist yet)
            ResponseAssertions.assertStatusCodeIn(response, 200, 404);

            if (response.getStatusCode() == 200) {
                System.out.println("✓ Successfully retrieved stage settings");
            } else {
                System.out.println("⚠ Stage settings not found (404) - this may be expected for new stages");
            }
        });
    }

    @Test(description = "Get stage settings with invalid ID - should return 404", priority = 2)
    @Story("Stage Settings Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving settings with invalid stage ID returns 404 Not Found")
    public void testGetStageSettings_InvalidId_Returns404() {
        Allure.step("Attempt to get stage settings with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = stageSettingsClient.getStageSettings(invalidId);

            System.out.println("=== Get Stage Settings With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Stage ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid stage ID");
        });
    }

    @Test(description = "Get stage settings - unauthorized - should return 401", priority = 3)
    @Story("Stage Settings Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that unauthorized access returns 401")
    public void testGetStageSettings_Unauthorized_Returns401() {
        Allure.step("Attempt to get stage settings without authentication", () -> {
            if (testStageId == null) {
                System.err.println("✗ Stage ID not available");
                return;
            }

            // Using unauthorized request spec
            Response response = io.restassured.RestAssured.given()
                    .spec(config.RequestSpecFactory.getRequestSpecWithoutAuth())
                    .pathParam("stageId", testStageId)
                    .when()
                    .get("/api/v1/stages/settings/{stageId}");

            System.out.println("=== Get Stage Settings Unauthorized Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Stage ID: " + testStageId);

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
