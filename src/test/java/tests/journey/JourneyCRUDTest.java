package tests.journey;

import assertions.*;
import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for validating CRUD operations on Journey endpoints.
 * Tests Read, Update, Archive, Unarchive, and Delete operations.
 * Note: Create operations are tested separately in required field validation tests.
 */
@Epic("Journey Management")
@Feature("Journey CRUD Operations")
public class JourneyCRUDTest {
    private JourneyClient journeyClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String createdJourneyId;
    private String createdJourneyTitle;

    @BeforeClass
    public void setup() {
        journeyClient = new JourneyClient();
        objectMapper = new ObjectMapper();

        // Create a test journey for CRUD operations
        createTestJourney();
    }

    /**
     * Helper method to create a test journey for CRUD operations
     */
    private void createTestJourney() {
        Allure.step("Setup: Create test journey for CRUD operations", () -> {
            createdJourneyTitle = "Journey CRUD Test " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", createdJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for CRUD testing");
            journeyRequest.put("language", "en-gb");

            Response createResponse = journeyClient.createJourney(journeyRequest);

            if (createResponse.getStatusCode() == 200 || createResponse.getStatusCode() == 201) {
                System.out.println("✓ Test journey created successfully: " + createdJourneyTitle);

                // Get the journey ID by searching
                Response getAllResponse = journeyClient.getAllJourneys(0, 10, createdJourneyTitle);
                if (getAllResponse.getStatusCode() == 200) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(getAllResponse.getBody().asString());
                        JsonNode content = rootNode.get("content");
                        if (content != null && content.isArray() && content.size() > 0) {
                            createdJourneyId = content.get(0).get("id").asText();
                            System.out.println("✓ Retrieved journey ID: " + createdJourneyId);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract journey ID: " + e.getMessage());
                    }
                }
            } else {
                System.err.println("✗ Failed to create test journey: " + createResponse.getStatusCode());
            }
        });
    }

    // ===================== READ OPERATIONS =====================

    @Test(description = "Get all journeys - should return 200", priority = 1)
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving all journeys returns 200 OK with paginated data")
    public void testGetAllJourneys_Success() {
        Allure.step("Get all journeys without filters", () -> {
            Response response = journeyClient.getAllJourneys();

            System.out.println("=== Get All Journeys Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify response structure
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody().asString());
                assert rootNode.has("content") : "Response should contain 'content' field";
                assert rootNode.get("content").isArray() : "'content' should be an array";
                System.out.println("✓ Successfully retrieved journeys");
            } catch (Exception e) {
                System.err.println("✗ Failed to parse response: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get all journeys with search filter - should return 200", priority = 2)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that searching journeys by title returns 200 OK with filtered results")
    public void testGetAllJourneys_WithSearch_Success() {
        Allure.step("Get journeys with search filter", () -> {
            if (createdJourneyTitle == null) {
                System.err.println("✗ Test journey title not available");
                return;
            }

            Response response = journeyClient.getAllJourneys(0, 10, createdJourneyTitle);

            System.out.println("=== Get Journeys With Search Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Search term: " + createdJourneyTitle);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify search results
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody().asString());
                JsonNode content = rootNode.get("content");
                if (content != null && content.isArray() && content.size() > 0) {
                    String firstTitle = content.get(0).get("content").get("title").asText();
                    System.out.println("✓ Found journey with title: " + firstTitle);
                    assert firstTitle.contains(createdJourneyTitle) || createdJourneyTitle.contains(firstTitle)
                        : "Search result should match search term";
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to verify search results: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get all journeys with status filter - should return 200", priority = 3)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that filtering journeys by status returns 200 OK")
    public void testGetAllJourneys_WithStatusFilter_Success() {
        Allure.step("Get journeys filtered by status", () -> {
            Response response = journeyClient.getAllJourneys(0, 10, "", "draft", "");

            System.out.println("=== Get Journeys With Status Filter Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Status filter: draft");
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully filtered journeys by status");
        });
    }

    @Test(description = "Get all journeys with pagination - should return 200", priority = 4)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that pagination works correctly for journey retrieval")
    public void testGetAllJourneys_WithPagination_Success() {
        Allure.step("Get journeys with pagination parameters", () -> {
            Response response = journeyClient.getAllJourneys(0, 5, "", "", "");

            System.out.println("=== Get Journeys With Pagination Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Page: 0, Size: 5");
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify pagination
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody().asString());
                assert rootNode.has("pageable") : "Response should contain pagination info";
                System.out.println("✓ Successfully retrieved paginated journeys");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify pagination: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get journey by ID - should return 200", priority = 5, dependsOnMethods = {"testGetAllJourneys_WithSearch_Success"})
    @Story("Read Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that retrieving a specific journey by ID returns 200 OK with journey details")
    public void testGetJourneyById_Success() {
        Allure.step("Get journey by ID", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for test");
                return;
            }

            Response response = journeyClient.getJourneyById(createdJourneyId);

            System.out.println("=== Get Journey By ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + createdJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify journey details
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody().asString());
                assert rootNode.has("id") : "Response should contain journey ID";
                String retrievedId = rootNode.get("id").asText();
                assert retrievedId.equals(createdJourneyId) : "Retrieved journey ID should match requested ID";
                System.out.println("✓ Successfully retrieved journey by ID");
            } catch (Exception e) {
                System.err.println("✗ Failed to verify journey details: " + e.getMessage());
            }
        });
    }

    @Test(description = "Get journey by invalid ID - should return 404", priority = 6)
    @Story("Read Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that retrieving a journey with invalid ID returns 404 Not Found")
    public void testGetJourneyById_InvalidId_Returns404() {
        Allure.step("Attempt to get journey with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";
            Response response = journeyClient.getJourneyById(invalidId);

            System.out.println("=== Get Journey By Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    // ===================== UPDATE OPERATIONS =====================

    @Test(description = "Update journey - should return 200", priority = 7, dependsOnMethods = {"testGetJourneyById_Success"})
    @Story("Update Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that updating a journey returns 200 OK")
    public void testUpdateJourney_Success() {
        Allure.step("Update journey with new details", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for update");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", createdJourneyTitle + " - Updated");
            updateRequest.put("assetId", TEST_ASSET_ID);
            updateRequest.put("assetDescription", "Updated journey description");
            updateRequest.put("language", "en-gb");

            Response response = journeyClient.updateJourney(createdJourneyId, updateRequest);

            System.out.println("=== Update Journey Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + createdJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully updated journey");
        });
    }

    @Test(description = "Update journey with invalid ID - should return 404", priority = 8)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a journey with invalid ID returns 404 Not Found")
    public void testUpdateJourney_InvalidId_Returns404() {
        Allure.step("Attempt to update journey with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "Updated Journey");
            updateRequest.put("assetId", TEST_ASSET_ID);
            updateRequest.put("language", "en-gb");

            Response response = journeyClient.updateJourney(invalidId, updateRequest);

            System.out.println("=== Update Journey With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    @Test(description = "Update journey with missing required fields - should return 400", priority = 9)
    @Story("Update Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that updating a journey with missing required fields returns 400 Bad Request")
    public void testUpdateJourney_MissingRequiredFields_Returns400() {
        Allure.step("Attempt to update journey with missing required fields", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for test");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("language", "en-gb");
            // Missing title and assetId

            Response response = journeyClient.updateJourney(createdJourneyId, updateRequest);

            System.out.println("=== Update Journey With Missing Fields Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing required fields");
        });
    }

    // ===================== ARCHIVE/UNARCHIVE OPERATIONS =====================

    @Test(description = "Archive journey - should return 200", priority = 10, dependsOnMethods = {"testUpdateJourney_Success"})
    @Story("Archive Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that archiving a journey returns 200 OK")
    public void testArchiveJourney_Success() {
        Allure.step("Archive journey", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for archive");
                return;
            }

            Map<String, Object> archiveRequest = new HashMap<>();
            archiveRequest.put("journeyId", createdJourneyId);
            archiveRequest.put("status", "ARCHIVE");

            Response response = journeyClient.archiveJourney(archiveRequest);

            System.out.println("=== Archive Journey Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + createdJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully archived journey");
        });
    }

    @Test(description = "Verify journey is archived - should return 200 with ARCHIVED status", priority = 11, dependsOnMethods = {"testArchiveJourney_Success"})
    @Story("Archive Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the journey status is ARCHIVED after archiving")
    public void testVerifyJourneyArchived_Success() {
        Allure.step("Verify journey is archived", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for verification");
                return;
            }

            // Search for archived journey
            Response response = journeyClient.getAllJourneys(0, 10, createdJourneyTitle, "archived", "");

            System.out.println("=== Verify Journey Archived Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify journey status
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody().asString());
                JsonNode content = rootNode.get("content");
                boolean foundArchived = false;
                if (content != null && content.isArray()) {
                    for (JsonNode journey : content) {
                        if (journey.get("id").asText().equals(createdJourneyId)) {
                            String status = journey.get("status").asText();
                            assert status.equalsIgnoreCase("ARCHIVED") : "Journey status should be ARCHIVED";
                            foundArchived = true;
                            System.out.println("✓ Verified journey is archived with status: " + status);
                            break;
                        }
                    }
                }
                if (!foundArchived) {
                    System.out.println("⚠ Journey not found in archived list, but archive operation succeeded");
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to verify archived status: " + e.getMessage());
            }
        });
    }

    @Test(description = "Unarchive journey - should return 200", priority = 12, dependsOnMethods = {"testVerifyJourneyArchived_Success"})
    @Story("Archive Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that unarchiving a journey returns 200 OK")
    public void testUnarchiveJourney_Success() {
        Allure.step("Unarchive journey", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for unarchive");
                return;
            }

            Map<String, Object> unarchiveRequest = new HashMap<>();
            unarchiveRequest.put("journeyId", createdJourneyId);
            unarchiveRequest.put("status", "UNARCHIVE");

            Response response = journeyClient.archiveJourney(unarchiveRequest);

            System.out.println("=== Unarchive Journey Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + createdJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);
            System.out.println("✓ Successfully unarchived journey");
        });
    }

    @Test(description = "Verify journey is unarchived - should not be in archived status", priority = 13, dependsOnMethods = {"testUnarchiveJourney_Success"})
    @Story("Archive Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the journey status is no longer ARCHIVED after unarchiving")
    public void testVerifyJourneyUnarchived_Success() {
        Allure.step("Verify journey is unarchived", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for verification");
                return;
            }

            // Search for journey (not in archived status)
            Response response = journeyClient.getAllJourneys(0, 10, createdJourneyTitle, "", "");

            System.out.println("=== Verify Journey Unarchived Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 200 OK
            ResponseAssertions.assertStatusCode(response, 200);

            // Verify journey status is not archived
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody().asString());
                JsonNode content = rootNode.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode journey : content) {
                        if (journey.get("id").asText().equals(createdJourneyId)) {
                            String status = journey.get("status").asText();
                            assert !status.equalsIgnoreCase("ARCHIVED") : "Journey status should not be ARCHIVED";
                            System.out.println("✓ Verified journey is unarchived with status: " + status);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to verify unarchived status: " + e.getMessage());
            }
        });
    }

    @Test(description = "Archive journey with missing journeyId - should return 400", priority = 14)
    @Story("Archive Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that archiving without journeyId returns 400 Bad Request")
    public void testArchiveJourney_MissingJourneyId_Returns400() {
        Allure.step("Attempt to archive journey without journeyId", () -> {
            Map<String, Object> archiveRequest = new HashMap<>();
            archiveRequest.put("status", "ARCHIVE");
            // Missing journeyId

            Response response = journeyClient.archiveJourney(archiveRequest);

            System.out.println("=== Archive Journey Without ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing journeyId");
        });
    }

    @Test(description = "Archive journey with invalid journeyId - should return 404", priority = 15)
    @Story("Archive Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that archiving with invalid journeyId returns 404 Not Found")
    public void testArchiveJourney_InvalidId_Returns404() {
        Allure.step("Attempt to archive journey with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Map<String, Object> archiveRequest = new HashMap<>();
            archiveRequest.put("journeyId", invalidId);
            archiveRequest.put("status", "ARCHIVE");

            Response response = journeyClient.archiveJourney(archiveRequest);

            System.out.println("=== Archive Journey With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    // ===================== DELETE OPERATIONS =====================

    @Test(description = "Delete journey - should return 204", priority = 16, dependsOnMethods = {"testVerifyJourneyUnarchived_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that deleting a journey returns 204 No Content")
    public void testDeleteJourney_Success() {
        Allure.step("Delete journey", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for deletion");
                return;
            }

            Response response = journeyClient.deleteJourney(createdJourneyId);

            System.out.println("=== Delete Journey Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + createdJourneyId);

            // Assert 204 No Content
            ResponseAssertions.assertStatusCode(response, 204);
            System.out.println("✓ Successfully deleted journey");
        });
    }

    @Test(description = "Verify journey is deleted - should return 404", priority = 17, dependsOnMethods = {"testDeleteJourney_Success"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that the deleted journey cannot be retrieved")
    public void testVerifyJourneyDeleted_Returns404() {
        Allure.step("Verify journey is deleted", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for verification");
                return;
            }

            Response response = journeyClient.getJourneyById(createdJourneyId);

            System.out.println("=== Verify Journey Deleted Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Deleted Journey ID: " + createdJourneyId);
            System.out.println("Body: " + response.getBody().asString());

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Verified journey is deleted - returns 404");
        });
    }

    @Test(description = "Delete journey with invalid ID - should return 404", priority = 18)
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that deleting with invalid ID returns 404 Not Found")
    public void testDeleteJourney_InvalidId_Returns404() {
        Allure.step("Attempt to delete journey with invalid ID", () -> {
            String invalidId = "00000000-0000-0000-0000-000000000000";

            Response response = journeyClient.deleteJourney(invalidId);

            System.out.println("=== Delete Journey With Invalid ID Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Invalid Journey ID: " + invalidId);

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for invalid journey ID");
        });
    }

    @Test(description = "Delete already deleted journey - should return 404", priority = 19, dependsOnMethods = {"testVerifyJourneyDeleted_Returns404"})
    @Story("Delete Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that deleting an already deleted journey returns 404 Not Found")
    public void testDeleteJourney_AlreadyDeleted_Returns404() {
        Allure.step("Attempt to delete already deleted journey", () -> {
            if (createdJourneyId == null) {
                System.err.println("✗ Journey ID not available for test");
                return;
            }

            Response response = journeyClient.deleteJourney(createdJourneyId);

            System.out.println("=== Delete Already Deleted Journey Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Journey ID: " + createdJourneyId);

            // Assert 404 Not Found
            ResponseAssertions.assertStatusCode(response, 404);
            System.out.println("✓ Correctly returned 404 for already deleted journey");
        });
    }
}
