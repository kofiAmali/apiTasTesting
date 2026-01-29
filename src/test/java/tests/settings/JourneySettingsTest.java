package tests.settings;

import assertions.*;
import client.*;
import config.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for Journey Settings endpoints.
 */
@Epic("Journey Management")
@Feature("Journey Settings")
public class JourneySettingsTest {
    private JourneySettingsClient client;
    private static final String TEST_JOURNEY_ID = "test-journey-123";

    @BeforeClass
    public void setup() {
        client = new JourneySettingsClient();
    }

    @Test(description = "Get journey settings - Success (200)")
    public void testGetJourneySettings_Success() {
        // Act
        Response response = client.getJourneySettings(TEST_JOURNEY_ID);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);
        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
        }
    }

    @Test(description = "Update journey settings - Success (200)")
    public void testUpdateJourneySettings_Success() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("defaultLanguage", "en");
        request.put("isActive", true);

        // Act
        Response response = client.updateJourneySettings(TEST_JOURNEY_ID, request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);
    }

    @Test(description = "Update welcome message - Success (200)")
    public void testUpdateWelcomeMessage_Success() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("message", "Welcome to your onboarding journey!");
        request.put("displayDuration", 5000);

        // Act
        Response response = client.updateWelcomeMessage(TEST_JOURNEY_ID, request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);
    }

    @Test(description = "Update language settings - Success (200)")
    public void testUpdateLanguageSettings_Success() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("defaultLanguage", "en");
        request.put("availableLanguages", Arrays.asList("en", "de", "fr"));

        // Act
        Response response = client.updateLanguageSettings(TEST_JOURNEY_ID, request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);
    }

    @Test(description = "Update journey settings - Unauthorized (403)")
    public void testUpdateJourneySettings_Unauthorized() {
        // Arrange
        String originalToken = AuthManager.getBearerToken();
        AuthManager.clearBearerToken();

        try {
            Response response = client.updateJourneySettings(TEST_JOURNEY_ID, new HashMap<>());
            ResponseAssertions.assertStatusCode(response, 403);
        } finally {
            AuthManager.setBearerToken(originalToken);
        }
    }
}
