package tests.pages;

import client.*;
import com.fasterxml.jackson.databind.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Test class for creating different types of pages.
 * Tests all available page template types based on Postman collection.
 */
@Epic("Page Management")
@Feature("Page Type Creation Tests")
public class PageTypeCreationTest {
    private PageClient pageClient;
    private JourneyClient journeyClient;
    private StageClient stageClient;
    private ChapterClient chapterClient;
    private ObjectMapper objectMapper;

    // Test data
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";
    private String testJourneyId;
    private String testJourneySlug;
    private String testChapterSlug;

    @BeforeClass
    public void setup() {
        pageClient = new PageClient();
        journeyClient = new JourneyClient();
        stageClient = new StageClient();
        chapterClient = new ChapterClient();
        objectMapper = new ObjectMapper();

        setupTestData();
    }

    /**
     * Setup test journey, stage, and chapter for page creation
     */
    private void setupTestData() {
        Allure.step("Setup: Create test journey, stage, and chapter for pages", () -> {
            // 1. Create test journey
            String testJourneyTitle = "Page Types Test Journey " + System.currentTimeMillis();

            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", testJourneyTitle);
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Journey for page type testing");
            journeyRequest.put("language", "en-gb");

            Response createJourneyResponse = journeyClient.createJourney(journeyRequest);

            if (createJourneyResponse.getStatusCode() == 200 || createJourneyResponse.getStatusCode() == 201) {
                System.out.println("✓ Test journey created: " + testJourneyTitle);

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
                String testStageTitle = "Page Types Stage " + System.currentTimeMillis();

                Map<String, Object> stageRequest = new HashMap<>();
                stageRequest.put("title", testStageTitle);
                stageRequest.put("assetId", TEST_ASSET_ID);
                stageRequest.put("assetDescription", "Stage for page type testing");
                stageRequest.put("status", "DRAFT");
                stageRequest.put("language", "en-gb");

                Response createStageResponse = stageClient.createStage(testJourneySlug, stageRequest);

                if (createStageResponse.getStatusCode() == 200 || createStageResponse.getStatusCode() == 201) {
                    System.out.println("✓ Test stage created");

                    // Extract stage slug directly from the create response
                    String stageSlug = null;
                    try {
                        JsonNode stageResponseNode = objectMapper.readTree(createStageResponse.getBody().asString());
                        if (stageResponseNode.has("message")) {
                            stageSlug = stageResponseNode.get("message").asText();
                            System.out.println("✓ Retrieved stage slug: " + stageSlug);
                        } else {
                            System.err.println("⚠ Stage created but 'message' field not found in response");
                            System.err.println("Response body: " + createStageResponse.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Failed to extract stage slug: " + e.getMessage());
                        e.printStackTrace();
                    }

                    // 3. Create test chapter
                    if (stageSlug != null) {
                        String testChapterTitle = "Page Types Chapter " + System.currentTimeMillis();

                        Map<String, Object> chapterRequest = new HashMap<>();
                        chapterRequest.put("title", testChapterTitle);
                        chapterRequest.put("assetId", TEST_ASSET_ID);
                        chapterRequest.put("assetDescription", "Chapter for page type testing");
                        chapterRequest.put("status", "DRAFT");
                        chapterRequest.put("language", "en-gb");

                        Response createChapterResponse = chapterClient.createChapter(stageSlug, chapterRequest);

                        if (createChapterResponse.getStatusCode() == 200 || createChapterResponse.getStatusCode() == 201) {
                            System.out.println("✓ Test chapter created");

                            // Extract chapter slug directly from the create response
                            try {
                                JsonNode chapterResponseNode = objectMapper.readTree(createChapterResponse.getBody().asString());
                                if (chapterResponseNode.has("message")) {
                                    testChapterSlug = chapterResponseNode.get("message").asText();
                                    System.out.println("✓ Retrieved chapter slug: " + testChapterSlug);
                                } else {
                                    System.err.println("⚠ Chapter created but 'slug' field not found in response");
                                    System.err.println("Response body: " + createChapterResponse.getBody().asString());
                                }
                            } catch (Exception e) {
                                System.err.println("✗ Failed to extract chapter slug: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("✗ Chapter creation failed with status: " + createChapterResponse.getStatusCode());
                            System.err.println("Response body: " + createChapterResponse.getBody().asString());
                        }
                    }
                }
            }
        });
    }

    // ==================== PAGE TYPE CREATION TESTS ====================

    @Test(description = "Create Image Template Page - oba_image_template", priority = 1)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates creation of Image template page type")
    public void testCreateImagePage_Success() {
        Allure.step("Create Image Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_image_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_image_template");
            content.put("title", "Image Template Test Page");
            content.put("imageId", TEST_ASSET_ID);
            content.put("information", createRichTextContent("Image page additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Image Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Image template page");
        });
    }

    @Test(description = "Create Text with Image Template Page - oba_text_image_template", priority = 2)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates creation of Text with Image template page type")
    public void testCreateTextWithImagePage_Success() {
        Allure.step("Create Text with Image Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_text_image_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_text_image_template");
            content.put("title", "Text with Image Template Test");
            content.put("text", createRichTextWithFormatting());
            content.put("imageId", TEST_ASSET_ID);
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Text with Image Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Text with Image template page");
        });
    }

    @Test(description = "Create Text Template Page - oba_text_template", priority = 3)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates creation of Text template page type")
    public void testCreateTextPage_Success() {
        Allure.step("Create Text Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_text_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_text_template");
            content.put("title", "Text Template Test Page");
            content.put("text", createRichTextWithFormatting());
            content.put("description", createRichTextContent("Page description"));
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Text Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Text template page");
        });
    }

    @Test(description = "Create Quiz Template Page - oba_quiz_template", priority = 4)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates creation of Quiz template page type")
    public void testCreateQuizPage_Success() {
        Allure.step("Create Quiz Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_quiz_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_quiz_template");
            content.put("title", "Multiple Choice Quiz Test");
            content.put("question", createRichTextContent("What is the capital of Germany?"));

            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createQuizAnswer("Berlin", true));
            answers.add(createQuizAnswer("Munich", false));
            answers.add(createQuizAnswer("Hamburg", false));
            content.put("answers", answers);

            content.put("correctExplanation", createRichTextContent("Correct! Berlin is the capital."));
            content.put("incorrectExplanation", createRichTextContent("Not quite, try again."));
            content.put("information", createRichTextContent("Additional quiz information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Quiz Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Quiz template page");
        });
    }

    @Test(description = "Create Image Quiz Template Page - oba_image_quiz_template", priority = 5)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates creation of Image Quiz template page type")
    public void testCreateImageQuizPage_Success() {
        Allure.step("Create Image Quiz Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_image_quiz_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_image_quiz_template");
            content.put("title", "Image Quiz Test Page");
            content.put("question", createRichTextContent("Which image represents the correct concept?"));

            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createImageQuizAnswer(TEST_ASSET_ID, "Option A", true));
            answers.add(createImageQuizAnswer(TEST_ASSET_ID, "Option B", false));
            answers.add(createImageQuizAnswer(TEST_ASSET_ID, "Option C", false));
            content.put("answers", answers);

            content.put("correctExplanation", createRichTextContent("Correct! Great job!"));
            content.put("incorrectExplanation", createRichTextContent("Not quite, try again."));
            content.put("information", createRichTextContent("Additional quiz information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Image Quiz Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Image Quiz template page");
        });
    }

    @Test(description = "Create Order Image Template Page - oba_order_image_template", priority = 6)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates creation of Order Image template page type")
    public void testCreateOrderImagePage_Success() {
        Allure.step("Create Order Image Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_order_image_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_order_image_template");
            content.put("title", "Order Image Test Page");

            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createOrderImageAnswer("First step", TEST_ASSET_ID));
            answers.add(createOrderImageAnswer("Second step", TEST_ASSET_ID));
            content.put("answers", answers);

            content.put("correctExplanation", createRichTextContent("Correct ordering!"));
            content.put("incorrectExplanation", createRichTextContent("Not quite right"));
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Order Image Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Order Image template page");
        });
    }

    @Test(description = "Create Ranking Template Page - oba_ranking_template", priority = 7)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates creation of Ranking template page type")
    public void testCreateRankingPage_Success() {
        Allure.step("Create Ranking Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_ranking_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_ranking_template");
            content.put("title", "Ranking Template Test");

            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createRankingAnswer("Option A", "Description for A"));
            answers.add(createRankingAnswer("Option B", "Description for B"));
            content.put("answers", answers);

            content.put("correctExplanation", createRichTextContent("Great ranking!"));
            content.put("incorrectExplanation", createRichTextContent("Try again"));
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Ranking Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Ranking template page");
        });
    }

    @Test(description = "Create Essay Template Page - oba_essay_template", priority = 8)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates creation of Essay template page type")
    public void testCreateEssayPage_Success() {
        Allure.step("Create Essay Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_essay_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_essay_template");
            content.put("title", "Essay Template Test Page");
            content.put("essayLabel", "Your Response");
            content.put("essayPlaceholder", "Enter your essay here...");
            content.put("isPhotoEssay", false);
            content.put("explanation", createRichTextContent("Essay explanation text"));
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Essay Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Essay template page");
        });
    }

    @Test(description = "Create Likert Scale Template Page - oba_likert_scale_template", priority = 9)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates creation of Likert Scale template page type")
    public void testCreateLikertScalePage_Success() {
        Allure.step("Create Likert Scale Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_likert_scale_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_likert_scale_template");
            content.put("title", "Likert Scale Test");
            content.put("description", createRichTextContent("Rate the following statements"));

            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createLikertAnswer("Statement 1", true));
            answers.add(createLikertAnswer("Statement 2", false));
            content.put("answers", answers);

            content.put("explanation", createRichTextContent("Thank you for your responses"));
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Likert Scale Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Likert Scale template page");
        });
    }

    @Test(description = "Create Video with Text Template Page - oba_text_video_template", priority = 10)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates creation of Video with Text template page type")
    public void testCreateVideoWithTextPage_Success() {
        Allure.step("Create Video with Text Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_text_video_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_text_video_template");
            content.put("title", "Video Template Test Page");
            content.put("description", createRichTextContent("Video description content"));
            content.put("videoUrl", "https://example.com/video.mp4");
            content.put("subtitleUrl", "https://example.com/subtitles.srt");
            content.put("videoDescription", "Introduction video");
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Video with Text Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Video with Text template page");
        });
    }

    @Test(description = "Create Embedded Content Template Page - oba_embed_template", priority = 11)
    @Story("Page Type Creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates creation of Embedded Content template page type")
    public void testCreateEmbeddedContentPage_Success() {
        Allure.step("Create Embedded Content Template Page", () -> {
            if (testChapterSlug == null) {
                System.err.println("✗ Chapter slug not available");
                return;
            }

            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_embed_template");
            pageRequest.put("chapterSlug", testChapterSlug);
            pageRequest.put("language", "en");
            pageRequest.put("includeInPublishing", true);

            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_embed_template");
            content.put("title", "Embedded Content Test Page");
            content.put("description", createRichTextContent("Description of embedded content"));
            content.put("embeddedContent", createEmbeddedHTML());
            content.put("information", createRichTextContent("Additional information"));

            pageRequest.put("content", content);

            Response response = pageClient.createPage(pageRequest);

            System.out.println("=== Create Embedded Content Page Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody().asString());

            assert response.getStatusCode() == 200 || response.getStatusCode() == 201 :
                "Expected 200/201 but got " + response.getStatusCode();
            System.out.println("✓ Successfully created Embedded Content template page");
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a simple rich text content structure
     */
    private Map<String, Object> createRichTextContent(String text) {
        Map<String, Object> richText = new HashMap<>();
        richText.put("type", "doc");

        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("type", "paragraph");

        List<Map<String, Object>> paragraphContent = new ArrayList<>();
        Map<String, Object> textNode = new HashMap<>();
        textNode.put("type", "text");
        textNode.put("text", text);
        paragraphContent.add(textNode);

        paragraph.put("content", paragraphContent);
        content.add(paragraph);
        richText.put("content", content);

        return richText;
    }

    /**
     * Creates rich text with formatting (bold, italic, lists, code)
     */
    private Map<String, Object> createRichTextWithFormatting() {
        Map<String, Object> richText = new HashMap<>();
        richText.put("type", "doc");

        List<Map<String, Object>> content = new ArrayList<>();

        // Bold text paragraph
        Map<String, Object> para1 = new HashMap<>();
        para1.put("type", "paragraph");
        List<Map<String, Object>> para1Content = new ArrayList<>();
        Map<String, Object> boldText = new HashMap<>();
        boldText.put("type", "text");
        boldText.put("text", "Hi there");
        List<Map<String, String>> marks = new ArrayList<>();
        marks.add(Collections.singletonMap("type", "bold"));
        boldText.put("marks", marks);
        para1Content.add(boldText);
        para1.put("content", para1Content);
        content.add(para1);

        // Bullet list
        Map<String, Object> bulletList = new HashMap<>();
        bulletList.put("type", "bullet_list");
        List<Map<String, Object>> listItems = new ArrayList<>();
        Map<String, Object> listItem = new HashMap<>();
        listItem.put("type", "list_item");
        List<Map<String, Object>> listItemContent = new ArrayList<>();
        Map<String, Object> listPara = new HashMap<>();
        listPara.put("type", "paragraph");
        List<Map<String, Object>> listParaContent = new ArrayList<>();
        Map<String, Object> listText = new HashMap<>();
        listText.put("type", "text");
        listText.put("text", "List item example");
        listParaContent.add(listText);
        listPara.put("content", listParaContent);
        listItemContent.add(listPara);
        listItem.put("content", listItemContent);
        listItems.add(listItem);
        bulletList.put("content", listItems);
        content.add(bulletList);

        richText.put("content", content);
        return richText;
    }

    /**
     * Creates a quiz answer object
     */
    private Map<String, Object> createQuizAnswer(String text, boolean correct) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("text", text);
        answer.put("correct", correct);
        return answer;
    }

    /**
     * Creates an image quiz answer object
     */
    private Map<String, Object> createImageQuizAnswer(String assetId, String text, boolean correct) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("assetId", assetId);
        answer.put("text", text);
        answer.put("correct", correct);
        return answer;
    }

    /**
     * Creates an order image answer object
     */
    private Map<String, Object> createOrderImageAnswer(String answer, String assetId) {
        Map<String, Object> answerObj = new HashMap<>();
        answerObj.put("answer", answer);
        answerObj.put("assetId", assetId);
        return answerObj;
    }

    /**
     * Creates a ranking answer object
     */
    private Map<String, Object> createRankingAnswer(String answer, String description) {
        Map<String, Object> answerObj = new HashMap<>();
        answerObj.put("answer", answer);
        answerObj.put("description", description);
        return answerObj;
    }

    /**
     * Creates a Likert scale answer object
     */
    private Map<String, Object> createLikertAnswer(String text, boolean applicable) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("text", text);
        answer.put("applicable", applicable);
        return answer;
    }

    /**
     * Creates embedded HTML content
     */
    private Map<String, Object> createEmbeddedHTML() {
        Map<String, Object> embedded = new HashMap<>();
        embedded.put("type", "doc");

        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> codeBlock = new HashMap<>();
        codeBlock.put("type", "code_block");

        Map<String, String> attrs = new HashMap<>();
        attrs.put("class", "language-html");
        codeBlock.put("attrs", attrs);

        List<Map<String, Object>> codeContent = new ArrayList<>();
        Map<String, Object> textNode = new HashMap<>();
        textNode.put("type", "text");
        textNode.put("text", "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/example\" allowfullscreen></iframe>");
        codeContent.add(textNode);

        codeBlock.put("content", codeContent);
        content.add(codeBlock);
        embedded.put("content", content);

        return embedded;
    }

    @AfterClass
    public void cleanup() {
        Allure.step("Cleanup: Delete test journey (cascades to all created pages)", () -> {
            if (testJourneyId != null) {
                try {
                    Response deleteResponse = journeyClient.deleteJourney(testJourneyId);
                    if (deleteResponse.getStatusCode() == 204) {
                        System.out.println("✓ Cleanup: Test journey and all pages deleted");
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Cleanup: Failed to delete test journey: " + e.getMessage());
                }
            }
        });
    }
}
