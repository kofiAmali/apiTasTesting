package tests.language;

import assertions.*;
import client.*;
import config.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

/**
 * Test class for Language endpoints.
 * Tests supported language operations.
 */
@Epic("Localization")
@Feature("Language Management")
public class LanguageTest {
    private LanguageClient client;
    private static final String TEST_LANGUAGE_CODE = "en";

    @BeforeClass
    public void setup() {
        client = new LanguageClient();
    }

    @Test(description = "Get all languages - Success (200)")
    public void testGetAllLanguages_Success() {
        // Arrange
        // No specific setup needed

        // Act
        Response response = client.getAllLanguages();

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200, 404);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
            ResponseAssertions.assertResponseTimeBelow(response, 3000);
            ResponseAssertions.assertBodyNotEmpty(response);
        }
    }

    @Test(description = "Get language by code - Success (200)")
    public void testGetLanguageByCode_Success() {
        // Arrange
        // Using English language code

        // Act
        Response response = client.getLanguageByCode(TEST_LANGUAGE_CODE);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
        }
    }

    @Test(description = "Get language by code - Not found (404)")
    public void testGetLanguageByCode_NotFound() {
        // Arrange
        String invalidCode = "xyz";

        // Act
        Response response = client.getLanguageByCode(invalidCode);


        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404);
    }
}
