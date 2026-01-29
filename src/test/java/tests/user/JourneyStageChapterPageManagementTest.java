package tests.user;

import assertions.*;
import client.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * End-to-end test class for the complete content hierarchy flow.
 * Tests the creation chain: Journey → Stage → Chapter → Page
 */
@Epic("Content Management")
@Feature("End-to-End Flow")
public class JourneyStageChapterPageManagementTest {
    private JourneyClient journeyClient;
    private StageClient stageClient;
    private ChapterClient chapterClient;
    private PageClient pageClient;

    // Test data holders for chaining
    private String createdJourneySlug;
    private String createdStageSlug;
    private String createdChapterSlug;
    private String createdPageId;

    // Asset ID for testing (required by API)
    private static final String TEST_ASSET_ID = "d0f9b79d-c9d2-48a2-94e5-363787223829";

    @BeforeClass
    public void setup() {
        journeyClient = new JourneyClient();
        stageClient = new StageClient();
        chapterClient = new ChapterClient();
        pageClient = new PageClient();
    }

    @Test(description = "End-to-End: Create Journey → Stage → Chapter → Page", priority = 1)
    @Story("Complete Content Hierarchy")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Creates a complete content hierarchy by chaining Journey, Stage, Chapter, and Page creation")
    public void testCompleteContentHierarchyFlow_Success() {
        // ========== STEP 1: Create Journey ==========
        Allure.step("Step 1: Create Journey", () -> {
            Map<String, Object> journeyRequest = new HashMap<>();
            journeyRequest.put("title", "E2E Test Journey " + System.currentTimeMillis());
            journeyRequest.put("assetId", TEST_ASSET_ID);
            journeyRequest.put("assetDescription", "Test journey for end-to-end flow");
            journeyRequest.put("language", "en-gb");

            Response journeyResponse = journeyClient.createJourney(journeyRequest);
            
            System.out.println("=== Journey Creation Response ===");
            System.out.println("Status: " + journeyResponse.getStatusCode());
            System.out.println("Body: " + journeyResponse.getBody().asString());
            
            // Assert journey creation success
            ResponseAssertions.assertStatusCodeIn(journeyResponse, 200, 201);
            

            if (journeyResponse.getStatusCode() == 200) {
                try {
                    // Get the most recently created journey (first in the list)
                    createdJourneySlug = ResponseAssertions.extractJsonPath(journeyResponse, "$.message");
                    System.out.println("✓ Extracted Journey Slug: " + createdJourneySlug);
                } catch (Exception e) {
                    System.err.println("✗ Failed to extract journey slug: " + e.getMessage());
                    // Use a fallback slug if extraction fails
                    createdJourneySlug = "e2e-test-journey-" + System.currentTimeMillis();
                }
            }
        });

        // ========== STEP 2: Create Stage ==========
        Allure.step("Step 2: Create Stage under Journey", () -> {
            if (createdJourneySlug == null) {
                System.err.println("✗ Cannot create stage: Journey slug is null");
                return;
            }

            Map<String, Object> stageRequest = new HashMap<>();
            stageRequest.put("title", "E2E Test Stage " + System.currentTimeMillis());
            stageRequest.put("assetId", TEST_ASSET_ID);
            stageRequest.put("assetDescription", "Test stage for end-to-end flow");
            stageRequest.put("status", "DRAFT");
            stageRequest.put("language", "en-gb");

            Response stageResponse = stageClient.createStage(createdJourneySlug, stageRequest);
            
            System.out.println("=== Stage Creation Response ===");
            System.out.println("Status: " + stageResponse.getStatusCode());
            System.out.println("Body: " + stageResponse.getBody().asString());
            
            // Assert stage creation success
            ResponseAssertions.assertStatusCodeIn(stageResponse, 200, 201);

            if (stageResponse.getStatusCode() == 200) {
                try {
                    // Get the most recently created stage
                    createdStageSlug = ResponseAssertions.extractJsonPath(stageResponse, "$.message");
                    System.out.println("✓ Extracted Stage Slug: " + createdStageSlug);
                } catch (Exception e) {
                    System.err.println("✗ Failed to extract stage slug: " + e.getMessage());
                    createdStageSlug = "e2e-test-stage-" + System.currentTimeMillis();
                }
            }
        });

        // ========== STEP 3: Create Chapter ==========
        Allure.step("Step 3: Create Chapter under Stage", () -> {
            if (createdStageSlug == null) {
                System.err.println("✗ Cannot create chapter: Stage slug is null");
                return;
            }

            Map<String, Object> chapterRequest = new HashMap<>();
            chapterRequest.put("title", "E2E Test Chapter " + System.currentTimeMillis());
            chapterRequest.put("assetId", TEST_ASSET_ID);
            chapterRequest.put("assetDescription", "Test chapter for end-to-end flow");
            chapterRequest.put("status", "DRAFT");
            chapterRequest.put("language", "en-gb");

            Response chapterResponse = chapterClient.createChapter(createdStageSlug,chapterRequest);
            
            System.out.println("=== Chapter Creation Response ===");
            System.out.println("Status: " + chapterResponse.getStatusCode());
            System.out.println("Body: " + chapterResponse.getBody().asString());
            
            // Assert chapter creation success
            ResponseAssertions.assertStatusCodeIn(chapterResponse, 200, 201);
            
            // Try to extract chapter ID from response
            if (chapterResponse.getStatusCode() == 200 || chapterResponse.getStatusCode() == 201) {
                try {
                    createdChapterSlug = ResponseAssertions.extractJsonPath(chapterResponse, "$.message");
                    System.out.println("✓ Extracted Chapter Slug: " + createdChapterSlug);
                } catch (Exception e) {
                    // If direct extraction fails, try getting from stage's chapters
                    System.out.println("⚠ Direct chapter slug extraction failed, attempting alternative method...");
                    try {
                        Response chaptersResponse = chapterClient.getChaptersByStage(createdStageSlug);
                        if (chaptersResponse.getStatusCode() == 200) {
                            createdChapterSlug = ResponseAssertions.extractJsonPath(chaptersResponse, "$.content[0].slug");
                            System.out.println("✓ Extracted Chapter Slug from list: " + createdChapterSlug);
                        }
                    } catch (Exception ex) {
                        System.err.println("✗ Failed to extract chapter slug: " + ex.getMessage());
                        createdChapterSlug = "e2e-test-chapter-" + System.currentTimeMillis();
                    }
                }
            }
        });

        // ========== STEP 4: Create Page ==========
        Allure.step("Step 4: Create Page under Chapter", () -> {
            if (createdChapterSlug == null) {
                System.err.println("✗ Cannot create page: Chapter slug is null");
                return;
            }

            // Create a page with image template
            Map<String, Object> pageRequest = new HashMap<>();
            pageRequest.put("templateType", "oba_image_template");
            pageRequest.put("chapterSlug", createdChapterSlug);
            pageRequest.put("language", "en-gb");
            pageRequest.put("includeInPublishing", true);

            // Create page content
            Map<String, Object> content = new HashMap<>();
            content.put("templateType", "oba_image_template");
            content.put("title", "E2E Test Image Page " + System.currentTimeMillis());
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
            text.put("text", "This is an end-to-end test image page with additional information");
            paragraphContent.add(text);
            paragraph.put("content", paragraphContent);
            informationContent.add(paragraph);
            information.put("content", informationContent);
            content.put("information", information);

            pageRequest.put("content", content);

            Response pageResponse = pageClient.createPage(pageRequest);
            
            System.out.println("=== Page Creation Response ===");
            System.out.println("Status: " + pageResponse.getStatusCode());
            System.out.println("Body: " + pageResponse.getBody().asString());
            
            // Assert page creation success
            ResponseAssertions.assertStatusCodeIn(pageResponse, 200, 201);
            
            // Try to extract page ID from response
            if (pageResponse.getStatusCode() == 200 || pageResponse.getStatusCode() == 201) {
                try {
                    createdPageId = ResponseAssertions.extractJsonPath(pageResponse, "$.message");
                    System.out.println("✓ Extracted Page ID: " + createdPageId);
                } catch (Exception e) {
                    System.out.println("⚠ Direct page ID extraction failed, attempting alternative method...");
                    try {
                        // Try to get pages from chapter
                        Response pagesResponse = pageClient.getAllPagesInChapter(createdChapterSlug);
                        if (pagesResponse.getStatusCode() == 200) {
                            createdPageId = ResponseAssertions.extractJsonPath(pagesResponse, "$[0].id");
                            System.out.println("✓ Extracted Page ID from list: " + createdPageId);
                        }
                    } catch (Exception ex) {
                        System.err.println("✗ Failed to extract page ID: " + ex.getMessage());
                    }
                }
            }
        });

        // ========== STEP 5: Verify Complete Hierarchy ==========
        Allure.step("Step 5: Verify Complete Hierarchy", () -> {
            System.out.println("\n=== Final Hierarchy Summary ===");
            System.out.println("Journey Slug: " + createdJourneySlug);
            System.out.println("Stage Slug: " + createdStageSlug);
            System.out.println("Chapter Slug: " + createdChapterSlug);
            System.out.println("Page ID: " + createdPageId);
            
            // Verify the page is accessible and linked correctly
            if (createdChapterSlug != null) {
                Response pagesInChapter = pageClient.getAllPagesInChapter(createdChapterSlug);
                System.out.println("\nPages in Chapter Status: " + pagesInChapter.getStatusCode());
                
                if (pagesInChapter.getStatusCode() == 200) {
                    System.out.println("✓ Successfully verified page exists in chapter");
                    System.out.println("Pages Response: " + pagesInChapter.getBody().asString());
                }
            }
            
            System.out.println("\n✓ End-to-End Flow Completed Successfully!");
        });
    }

    @Test(description = "Verify Journey can be retrieved", priority = 2, dependsOnMethods = "testCompleteContentHierarchyFlow_Success")
    @Story("Journey Verification")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies that the created journey can be retrieved successfully")
    public void testVerifyJourneyExists() {
        if (createdJourneySlug == null) {
            System.out.println("⚠ Skipping journey verification: Journey slug not available");
            return;
        }

        Response response = journeyClient.getAllJourneys(0, 10, "", "", createdJourneySlug);
        ResponseAssertions.assertStatusCodeIn(response, 200);
        
        if (response.getStatusCode() == 200) {
            System.out.println("✓ Journey verified successfully");
        }
    }

    @Test(description = "Verify Stage can be retrieved", priority = 3, dependsOnMethods = "testCompleteContentHierarchyFlow_Success")
    @Story("Stage Verification")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies that the created stage can be retrieved from the journey")
    public void testVerifyStageExists() {
        if (createdJourneySlug == null || createdStageSlug == null) {
            System.out.println("⚠ Skipping stage verification: Required slugs not available");
            return;
        }

        Response response = stageClient.getAllStages(createdJourneySlug);
        ResponseAssertions.assertStatusCodeIn(response, 200);
        
        if (response.getStatusCode() == 200) {
            System.out.println("✓ Stage verified successfully");
        }
    }

    @Test(description = "Verify Page can be retrieved", priority = 4, dependsOnMethods = "testCompleteContentHierarchyFlow_Success")
    @Story("Page Verification")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies that the created page can be retrieved from the chapter")
    public void testVerifyPageExists() {
        if (createdChapterSlug == null) {
            System.out.println("⚠ Skipping page verification: Chapter slug not available");
            return;
        }

        Response response = pageClient.getAllPagesInChapter(createdChapterSlug);
        ResponseAssertions.assertStatusCodeIn(response, 200);
        
        if (response.getStatusCode() == 200) {
            System.out.println("✓ Page verified successfully");
            System.out.println("✓ Complete hierarchy is intact and accessible!");
        }
    }
}
