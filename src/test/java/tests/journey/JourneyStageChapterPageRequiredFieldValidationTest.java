package tests.journey;

import assertions.*;
import client.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for validating required fields in Journey, Stage, Chapter, and Page creation.
 * These tests verify that the API properly returns 400 Bad Request when required fields are missing.
 */
@Epic("API Validation")
@Feature("Required Field Validation")
public class JourneyStageChapterPageRequiredFieldValidationTest {
    private JourneyClient journeyClient;
    private StageClient stageClient;
    private ChapterClient chapterClient;
    private PageClient pageClient;

    // Valid test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private static final String TEST_JOURNEY_SLUG = "test-journey-slug";
    private static final String TEST_STAGE_SLUG = "test-stage-slug";
    private static final String TEST_CHAPTER_SLUG = "test-chapter-slug";

    @BeforeClass
    public void setup() {
        journeyClient = new JourneyClient();
        stageClient = new StageClient();
        chapterClient = new ChapterClient();
        pageClient = new PageClient();
    }

    // ===================== JOURNEY VALIDATION TESTS =====================

    @Test(description = "Create Journey without title - should return 400", priority = 1)
    @Story("Journey Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a journey without a title field returns 400 Bad Request")
    public void testCreateJourney_MissingTitle_Returns400() {
        Allure.step("Attempt to create Journey without title", () -> {
            Map<String, Object> journeyRequest = new HashMap<>();
            // Missing title
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Test journey without title");
            journeyRequest.put("language", "en-gb");

            Response response = journeyClient.createJourney(journeyRequest);

            System.out.println("=== Journey Creation Without Title Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing title");
        });
    }

    @Test(description = "Create Journey without assetId - should return 400", priority = 2)
    @Story("Journey Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a journey without an assetId field returns 400 Bad Request")
    public void testCreateJourney_MissingAssetId_Returns400() {
        Allure.step("Attempt to create Journey without assetId", () -> {
            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", "Test Journey " + System.currentTimeMillis());
            // Missing assetId
            journeyRequest.put("assetDescription", "Test journey without assetId");
            journeyRequest.put("language", "en-gb");

            Response response = journeyClient.createJourney(journeyRequest);

            System.out.println("=== Journey Creation Without AssetId Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing assetId");
        });
    }

    @Test(description = "Create Journey with empty title - should return 400", priority = 3)
    @Story("Journey Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a journey with an empty title returns 400 Bad Request")
    public void testCreateJourney_EmptyTitle_Returns400() {
        Allure.step("Attempt to create Journey with empty title", () -> {
            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", "");  // Empty title
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Test journey with empty title");
            journeyRequest.put("language", "en-gb");

            Response response = journeyClient.createJourney(journeyRequest);

            System.out.println("=== Journey Creation With Empty Title Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for empty title");
        });
    }

    @Test(description = "Create Journey with all required fields missing - should return 400", priority = 4)
    @Story("Journey Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that creating a journey with all required fields missing returns 400 Bad Request")
    public void testCreateJourney_AllRequiredFieldsMissing_Returns400() {
        Allure.step("Attempt to create Journey with all required fields missing", () -> {
            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("language", "en-gb");  // Only optional field

            Response response = journeyClient.createJourney(journeyRequest);

            System.out.println("=== Journey Creation With All Required Fields Missing Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for all required fields missing");
        });
    }

    // ===================== STAGE VALIDATION TESTS =====================

    @Test(description = "Create Stage without title - should return 400", priority = 5)
    @Story("Stage Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a stage without a title field returns 400 Bad Request")
    public void testCreateStage_MissingTitle_Returns400() {
        Allure.step("Attempt to create Stage without title", () -> {
            Map<String, Object> stageRequest = new HashMap<>();
            // Missing title
            stageRequest.put("assetId", TEST_ASSET_ID);
            stageRequest.put("assetDescription", "Test stage without title");
            stageRequest.put("status", "DRAFT");
            stageRequest.put("language", "en-gb");

            Response response = stageClient.createStage(TEST_JOURNEY_SLUG, stageRequest);

            System.out.println("=== Stage Creation Without Title Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing title");
        });
    }

    @Test(description = "Create Stage without assetId - should return 400", priority = 6)
    @Story("Stage Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a stage without an assetId field returns 400 Bad Request")
    public void testCreateStage_MissingAssetId_Returns400() {
        Allure.step("Attempt to create Stage without assetId", () -> {
            Map<String, Object> stageRequest = new HashMap<>();
            stageRequest.put("title", "Test Stage " + System.currentTimeMillis());
            // Missing assetId
            stageRequest.put("assetDescription", "Test stage without assetId");
            stageRequest.put("status", "DRAFT");
            stageRequest.put("language", "en-gb");

            Response response = stageClient.createStage(TEST_JOURNEY_SLUG, stageRequest);

            System.out.println("=== Stage Creation Without AssetId Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing assetId");
        });
    }

    @Test(description = "Create Stage with empty title - should return 400", priority = 7)
    @Story("Stage Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a stage with an empty title returns 400 Bad Request")
    public void testCreateStage_EmptyTitle_Returns400() {
        Allure.step("Attempt to create Stage with empty title", () -> {
            Map<String, Object> stageRequest = new HashMap<>();
            stageRequest.put("title", "");  // Empty title
            stageRequest.put("assetId", TEST_ASSET_ID);
            stageRequest.put("assetDescription", "Test stage with empty title");
            stageRequest.put("status", "DRAFT");
            stageRequest.put("language", "en-gb");

            Response response = stageClient.createStage(TEST_JOURNEY_SLUG, stageRequest);

            System.out.println("=== Stage Creation With Empty Title Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for empty title");
        });
    }

    @Test(description = "Create Stage with all required fields missing - should return 400", priority = 8)
    @Story("Stage Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that creating a stage with all required fields missing returns 400 Bad Request")
    public void testCreateStage_AllRequiredFieldsMissing_Returns400() {
        Allure.step("Attempt to create Stage with all required fields missing", () -> {
            Map<String, Object> stageRequest = new HashMap<>();
            stageRequest.put("status", "DRAFT");  // Only optional field

            Response response = stageClient.createStage(TEST_JOURNEY_SLUG, stageRequest);

            System.out.println("=== Stage Creation With All Required Fields Missing Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for all required fields missing");
        });
    }

    // ===================== CHAPTER VALIDATION TESTS =====================

    @Test(description = "Create Chapter without title - should return 400", priority = 9)
    @Story("Chapter Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a chapter without a title field returns 400 Bad Request")
    public void testCreateChapter_MissingTitle_Returns400() {
        Allure.step("Attempt to create Chapter without title", () -> {
            Map<String, Object> chapterRequest = new HashMap<>();
            // Missing title
            chapterRequest.put("assetId", TEST_ASSET_ID);
            chapterRequest.put("assetDescription", "Test chapter without title");
            chapterRequest.put("status", "DRAFT");
            chapterRequest.put("language", "en-gb");

            Response response = chapterClient.createChapter(TEST_STAGE_SLUG, chapterRequest);

            System.out.println("=== Chapter Creation Without Title Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing title");
        });
    }

    @Test(description = "Create Chapter without assetId - should return 400", priority = 10)
    @Story("Chapter Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a chapter without an assetId field returns 400 Bad Request")
    public void testCreateChapter_MissingAssetId_Returns400() {
        Allure.step("Attempt to create Chapter without assetId", () -> {
            Map<String, Object> chapterRequest = new HashMap<>();
            chapterRequest.put("title", "Test Chapter " + System.currentTimeMillis());
            // Missing assetId
            chapterRequest.put("assetDescription", "Test chapter without assetId");
            chapterRequest.put("status", "DRAFT");
            chapterRequest.put("language", "en-gb");

            Response response = chapterClient.createChapter(TEST_STAGE_SLUG, chapterRequest);

            System.out.println("=== Chapter Creation Without AssetId Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing assetId");
        });
    }

    @Test(description = "Create Chapter with empty title - should return 400", priority = 11)
    @Story("Chapter Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a chapter with an empty title returns 400 Bad Request")
    public void testCreateChapter_EmptyTitle_Returns400() {
        Allure.step("Attempt to create Chapter with empty title", () -> {
            Map<String, Object> chapterRequest = new HashMap<>();
            chapterRequest.put("title", "");  // Empty title
            chapterRequest.put("assetId", TEST_ASSET_ID);
            chapterRequest.put("assetDescription", "Test chapter with empty title");
            chapterRequest.put("status", "DRAFT");
            chapterRequest.put("language", "en-gb");

            Response response = chapterClient.createChapter(TEST_STAGE_SLUG, chapterRequest);

            System.out.println("=== Chapter Creation With Empty Title Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for empty title");
        });
    }

    @Test(description = "Create Chapter with all required fields missing - should return 400", priority = 12)
    @Story("Chapter Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that creating a chapter with all required fields missing returns 400 Bad Request")
    public void testCreateChapter_AllRequiredFieldsMissing_Returns400() {
        Allure.step("Attempt to create Chapter with all required fields missing", () -> {
            Map<String, Object> chapterRequest = new HashMap<>();
            chapterRequest.put("status", "DRAFT");  // Only optional field

            Response response = chapterClient.createChapter(TEST_STAGE_SLUG, chapterRequest);

            System.out.println("=== Chapter Creation With All Required Fields Missing Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for all required fields missing");
        });
    }

    // ===================== PAGE VALIDATION TESTS =====================

    @Test(description = "Create Page without templateType - should return 400", priority = 13)
    @Story("Page Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a page without a templateType field returns 400 Bad Request")
    public void testCreatePage_MissingTemplateType_Returns400() {
        Allure.step("Attempt to create Page without templateType", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            // Missing templateType
            pageRequest.put("chapterSlug", TEST_CHAPTER_SLUG);
            pageRequest.put("language", "en-gb");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("title", "Test Page");
            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Page Creation Without TemplateType Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing templateType");
        });
    }

    @Test(description = "Create Page without chapterSlug - should return 400", priority = 14)
    @Story("Page Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a page without a chapterSlug field returns 400 Bad Request")
    public void testCreatePage_MissingChapterSlug_Returns400() {
        Allure.step("Attempt to create Page without chapterSlug", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_quiz_template");
            // Missing chapterSlug
            pageRequest.put("language", "en-gb");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("title", "Test Page");
            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Page Creation Without ChapterSlug Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing chapterSlug");
        });
    }

    @Test(description = "Create Page without content - should return 400", priority = 15)
    @Story("Page Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a page without a content field returns 400 Bad Request")
    public void testCreatePage_MissingContent_Returns400() {
        Allure.step("Attempt to create Page without content", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_quiz_template");
            pageRequest.put("chapterSlug", TEST_CHAPTER_SLUG);
            pageRequest.put("language", "en-gb");
            pageRequest.put("includeInPublishing", true);
            // Missing content

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Page Creation Without Content Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing content");
        });
    }

    @Test(description = "Create Page with empty templateType - should return 400", priority = 16)
    @Story("Page Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a page with an empty templateType returns 400 Bad Request")
    public void testCreatePage_EmptyTemplateType_Returns400() {
        Allure.step("Attempt to create Page with empty templateType", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "");  // Empty templateType
            pageRequest.put("chapterSlug", TEST_CHAPTER_SLUG);
            pageRequest.put("language", "en-gb");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("title", "Test Page");
            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Page Creation With Empty TemplateType Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for empty templateType");
        });
    }

    @Test(description = "Create Page with empty chapterSlug - should return 400", priority = 17)
    @Story("Page Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a page with an empty chapterSlug returns 400 Bad Request")
    public void testCreatePage_EmptyChapterSlug_Returns400() {
        Allure.step("Attempt to create Page with empty chapterSlug", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_quiz_template");
            pageRequest.put("chapterSlug", "");  // Empty chapterSlug
            pageRequest.put("language", "en-gb");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("title", "Test Page");
            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Page Creation With Empty ChapterSlug Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for empty chapterSlug");
        });
    }

    @Test(description = "Create Page with all required fields missing - should return 400", priority = 18)
    @Story("Page Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates that creating a page with all required fields missing returns 400 Bad Request")
    public void testCreatePage_AllRequiredFieldsMissing_Returns400() {
        Allure.step("Attempt to create Page with all required fields missing", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("language", "en-gb");  // Only optional field
            pageRequest.put("includeInPublishing", true);  // Only optional field

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Page Creation With All Required Fields Missing Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for all required fields missing");
        });
    }

    // ===================== QUIZ TEMPLATE CONTENT VALIDATION TESTS =====================

    @Test(description = "Create Quiz Page with missing answers in content - should return 400", priority = 19)
    @Story("Page Content Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a quiz page without answers in content returns 400 Bad Request")
    public void testCreateQuizPage_MissingAnswers_Returns400() {
        Allure.step("Attempt to create Quiz Page without answers in content", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_quiz_template");
            pageRequest.put("chapterSlug", TEST_CHAPTER_SLUG);
            pageRequest.put("language", "en-gb");

            // Create content without answers
            Map<String, Object> content = new HashMap<>();
            content.put("title", "Quiz Without Answers");

            Map<String, Object> question = new HashMap<>();
            question.put("type", "doc");
            List<Map<String, Object>> questionContent = new ArrayList<>();
            Map<String, Object> paragraph = new HashMap<>();
            paragraph.put("type", "paragraph");
            List<Map<String, Object>> paragraphContent = new ArrayList<>();
            Map<String, Object> text = new HashMap<>();
            text.put("type", "text");
            text.put("text", "What is the question?");
            paragraphContent.add(text);
            paragraph.put("content", paragraphContent);
            questionContent.add(paragraph);
            question.put("content", questionContent);
            content.put("question", question);
            // Missing answers array

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Quiz Page Creation Without Answers Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for missing answers");
        });
    }

    @Test(description = "Create Quiz Page with insufficient answers (less than 2) - should return 400", priority = 20)
    @Story("Page Content Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a quiz page with less than 2 answers returns 400 Bad Request")
    public void testCreateQuizPage_InsufficientAnswers_Returns400() {
        Allure.step("Attempt to create Quiz Page with only 1 answer", () -> {
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_quiz_template");
            pageRequest.put("chapterSlug", TEST_CHAPTER_SLUG);
            pageRequest.put("language", "en-gb");

            Map<String, Object> content = new HashMap<>();
            content.put("title", "Quiz With One Answer");

            Map<String, Object> question = new HashMap<>();
            question.put("type", "doc");
            content.put("question", question);

            // Only 1 answer (minimum is 2)
            List<Map<String, Object>> answers = new ArrayList<>();
            Map<String, Object> answer1 = new HashMap<>();
            answer1.put("text", "Answer 1");
            answer1.put("isCorrect", true);
            answers.add(answer1);
            content.put("answers", answers);

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Quiz Page Creation With Insufficient Answers Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for insufficient answers (need at least 2)");
        });
    }

    @Test(description = "Create Page with invalid UUID format for assetId - should return 400", priority = 21)
    @Story("Journey Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that creating a journey with invalid UUID format for assetId returns 400 Bad Request")
    public void testCreateJourney_InvalidAssetIdFormat_Returns400() {
        Allure.step("Attempt to create Journey with invalid assetId format", () -> {
            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", "Test Journey " + System.currentTimeMillis());
            journeyRequest.put("assetId", "invalid-uuid-format");  // Invalid UUID
            journeyRequest.put("assetDescription", "Test journey with invalid assetId");
            journeyRequest.put("language", "en-gb");

            Response response = journeyClient.createJourney(journeyRequest);

            System.out.println("=== Journey Creation With Invalid AssetId Format Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            // Assert 400 Bad Request
            ResponseAssertions.assertStatusCode(response, 400);
            System.out.println("✓ Correctly returned 400 for invalid UUID format");
        });
    }
}
