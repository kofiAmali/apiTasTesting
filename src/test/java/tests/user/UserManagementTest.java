package tests.user;

import assertions.*;
import client.*;
import config.*;
import io.qameta.allure.*;
import io.restassured.response.*;
import org.testng.annotations.*;

/**
 * Comprehensive test class for ALL User Management endpoints.
 * Tests user CRUD, profile management, and open invitation operations.
 */
@Epic("User Management")
@Feature("User Operations")
public class UserManagementTest {
    private UserManagementClient client;
    private String createdUserId;
    private String createdInviteCodeId;
    private String openInviteId;
    private String userEmail;

    @BeforeClass
    public void setup() {
        client = new UserManagementClient();
    }

//    //========== GET ALL USERS TESTS ==========

    @Test(description = "Get all users - Success (200)")
    @Story("Get All Users")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that authenticated users can retrieve a list of all users successfully")
    public void testGetAllUsers_Success() {
        // Act
        Response response = client.getAllUsers();

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
            ResponseAssertions.assertResponseTimeBelow(response, 5000);
            ResponseAssertions.assertJsonPathExists(response, "$.content");
        }
    }

    @Test(description = "Get all users with filters - Success (200)")
    @Story("Get All Users")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that users can be filtered by role and status")
    public void testGetAllUsers_WithFilters() {
        // Act
        Response response = client.getAllUsers(0, 50, "john", "VIEWER", "ACTIVE");

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);
    }

    @Test(description = "Get all users - Unauthorized (403)")
    @Story("Get All Users")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that unauthenticated users cannot access the users list")
    public void testGetAllUsers_Unauthorized() {
        // Act
        Response response = client.getAllUsersWithoutToken();

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 403);
    }

    @Test(description = "Get all users - Invalid role (400)")
    @Story("Get All Users")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that invalid role parameter returns 400 error")
    public void testGetAllUsers_InvalidRole() {
        // Act
        Response response = client.getAllUsers(0, 50, "", "INVALID_ROLE", "");

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400);
    }

    // Need new Journey Id
    // ========== CREATE USER TESTS ==========

    @Test(description = "Create user with viewer role- Success (201)", priority = 1)
    public void testCreateUserViewer_Success() {
        // Arrange
        String request = "{\n" +
                "    \"firstName\": \"Some\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"roles\": \"VIEWER\",\n" +
                "    \"email\": \"newuser" + System.currentTimeMillis() + "@example.com\",\n" +
                "    \"data\": {\n" +
                "        \"roles\": \"VIEWER\",\n" +
                "        \"startDate\": \"30.06.2026\",\n" +
                "        \"deactivateAfter\": \"NINE_MONTHS\",\n" +
                "        \"journeyIds\": [\n" +
                "            \"3e888b3d-d390-4b39-ad52-a670394f8b3c\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // Act
        Response response = client.createUser(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 201);

        if (response.getStatusCode() == 201) {
            ResponseAssertions.assertGenericMessage(response);
        }
    }

    @Test(description = "Create user with ADMIN role- Success (201)", priority = 1)
    public void testCreateUserAdmin_Success() {
        // Arrange
        String request = "{\n" +
                "    \"firstName\": \"Some\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"roles\": \"ADMIN\",\n" +
                "    \"email\": \"newuser" + System.currentTimeMillis() + "@example.com\"\n" +
                "}";

        // Act
        Response response = client.createUser(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 201);

        if (response.getStatusCode() == 201) {
            ResponseAssertions.assertGenericMessage(response);
        }
    }

    @Test(description = "Create user with editor role- Success (201)", priority = 1)
    public void testCreateUserEditor_Success() {
        // Arrange
        String request = "{\n" +
                "    \"firstName\": \"Some\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"roles\": \"EDITOR\",\n" +
                "    \"email\": \"newuser" + System.currentTimeMillis() + "@example.com\",\n" +
                "    \"data\": {\n" +
                "        \"journeyIds\": [\n" +
                "            \"3e888b3d-d390-4b39-ad52-a670394f8b3c\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // Act
        Response response = client.createUser(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 201);

        if (response.getStatusCode() == 201) {
            ResponseAssertions.assertGenericMessage(response);
        }
    }



    @Test(description = "Create user - Invalid role (400)")
    public void testCreateUser_InvalidRole() {
        userEmail = "newuser" + System.currentTimeMillis() + "@example.com";
        // Arrange
        String request = "{\n" +
                "    \"firstName\": \"Some\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"roles\": \"INVALID_ROLE\",\n" +
                "    \"email\": \"" + userEmail + "\",\n" +
                "    \"data\": {\n" +
                "        \"journeyIds\": [\n" +
                "            \"3e888b3d-d390-4b39-ad52-a670394f8b3c\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";


        // Act
        Response response = client.createUser(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400);
    }

//    @Test(description = "Create user - Missing required field (400)")
//    public void testCreateUser_MissingEmail() {
//
//        // Arrange
//        String request = "{\n" +
//                "    \"firstName\": \"Some\",\n" +
//                "    \"lastName\": \"User\",\n" +
//                "    \"roles\": \"ADMIN\"\n" +
//                "}";
//
//        // Act
//        Response response = client.createUser(request);
//
//        // Assert
//        ResponseAssertions.assertStatusCodeIn(response, 400);
//    }

    @Test(description = "Create user - Duplicate email (409)")
    public void testCreateUser_DuplicateEmail() {
        // Arrange - Generate a unique email for this test
        userEmail = userEmail != null ? userEmail : "abc@example.com";

        String request = "{\n" +
                "    \"firstName\": \"Some\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"roles\": \"ADMIN\",\n" +
                "    \"email\": \"" + userEmail + "\"\n" +
                "}";

        // Act - Try to create twice
        client.createUser(request);
        Response response = client.createUser(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 409);
    }
//
    // ========== GET USER BY ID TESTS ==========

    @Test(description = "Get user by ID - Success (200)", priority = 2)
    @Story("Get User by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that a specific user can be retrieved by their ID")
    public void testGetUserById_Success() {
        // Arrange
        String userId = "b384b822-3081-701b-ea14-c492696ccc6f"; // Sample ID

        // Act
        Response response = client.getUserById(userId);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
            ResponseAssertions.assertJsonPathExists(response, "$.id");
            ResponseAssertions.assertJsonPathExists(response, "$.email");
        }
    }

    @Test(description = "Get user by ID - Not found (404)")
    public void testGetUserById_NotFound() {
        // Arrange
        String nonExistentId = "99999999-9999-9999-9999-999999999999";

        // Act
        Response response = client.getUserById(nonExistentId);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404);
    }

    // ========== UPDATE USER TESTS ==========

    @Test(description = "Setup: Create test user for update/archive operations", priority = 2)
    public void setupTestUserForUpdateAndArchive() {
        // Arrange - Create a unique test user
        userEmail = "testuser.update." + System.currentTimeMillis() + "@example.com";
        String request = "{\n" +
                "    \"firstName\": \"Test\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"roles\": \"VIEWER\",\n" +
                "    \"email\": \"" + userEmail + "\",\n" +
                "    \"data\": {\n" +
                "        \"roles\": \"VIEWER\",\n" +
                "        \"startDate\": \"30.06.2026\",\n" +
                "        \"deactivateAfter\": \"NINE_MONTHS\",\n" +
                "        \"journeyIds\": [\n" +
                "            \"3e888b3d-d390-4b39-ad52-a670394f8b3c\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // Act - Create the user
        Response createResponse = client.createUser(request);

        System.out.println("=== Create Test User Response ===");
        System.out.println("Status Code: " + createResponse.getStatusCode());
        System.out.println("Response Body: " + createResponse.getBody().asString());

        // Assert user was created
        ResponseAssertions.assertStatusCodeIn(createResponse, 201);

        if (createResponse.getStatusCode() == 201) {
            // Wait a moment for the user to be fully created in the system
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Get the user by email to extract the ID
            Response getByEmailResponse = client.getUserByEmail(userEmail);

            System.out.println("=== Get User By Email Response ===");
            System.out.println("Status Code: " + getByEmailResponse.getStatusCode());
            System.out.println("Response Body: " + getByEmailResponse.getBody().asString());

            if (getByEmailResponse.getStatusCode() == 200) {
                try {
                    // Extract the user ID from the search results
                    String jsonPath = "$.content[0].id";
                    createdUserId = ResponseAssertions.extractJsonPath(getByEmailResponse, jsonPath);
                    System.out.println("✓ Successfully created test user with ID: " + createdUserId);
                    System.out.println("✓ User email: " + userEmail);
                } catch (Exception e) {
                    System.err.println("✗ Could not extract user ID from response: " + e.getMessage());
                    System.err.println("Response: " + getByEmailResponse.getBody().asString());
                }
            } else {
                System.err.println("✗ Failed to retrieve user by email. Status: " + getByEmailResponse.getStatusCode());
            }
        }
    }

    @Test(description = "Update user - Success (200)", priority = 3, dependsOnMethods = "setupTestUserForUpdateAndArchive")
    public void testUpdateUser_Success() {
        // Arrange
        String userId = createdUserId != null ? createdUserId : "b384b822-3081-701b-ea14-c492696ccc6f";

        System.out.println("Updating user with ID: " + userId);

        String request = "{\n" +
                "    \"firstName\": \"Updated\",\n" +
                "    \"lastName\": \"TestUser\"\n" +
                "}";

        // Act
        Response response = client.updateUser(userId, request);

        System.out.println("=== Update User Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            System.out.println("✓ Successfully updated user");
        }
    }

    @Test(description = "Update user - Invalid role (400)")
    public void testUpdateUser_InvalidRole() {
        // Arrange - Use a known existing user ID or the created one
        String userId = createdUserId != null ? createdUserId : "b384b822-3081-701b-ea14-c492696ccc6f";

        String request = "{\n" +
                "    \"roles\": \"INVALID_ROLE\"\n" +
                "}";

        // Act
        Response response = client.updateUser(userId, request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 400);
    }

    //========== DELETE USER TESTS ==========

    @Test(description = "Delete user - Success (204)", priority = 7, dependsOnMethods = "testUnarchiveUser_Success")
    public void testDeleteUser_Success() {
        // Arrange - Use the created test user ID
        String userId = createdUserId != null ? createdUserId : "b384b822-3081-701b-ea14-c492696ccc6f";

        System.out.println("Deleting user with ID: " + userId);

        // Act
        Response response = client.deleteUser(userId);

        System.out.println("=== Delete User Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 204);

        if (response.getStatusCode() == 204) {
            System.out.println("✓ Successfully deleted user");
            // Clear the user ID since it's been deleted
            createdUserId = null;
            userEmail = null;
        }
    }

    @Test(description = "Delete user - Not found (404)")
    public void testDeleteUser_NotFound() {
        // Arrange
        String nonExistentId = "99999999-9999-9999-9999-999999999999";

        // Act
        Response response = client.deleteUser(nonExistentId);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 404);
    }

     //========== ARCHIVE USER TESTS ==========

    @Test(description = "Archive user - Success (200)", priority = 5, dependsOnMethods = "testUpdateUser_Success")
    public void testArchiveUser_Success() {
        // Arrange - Use the created test user ID
        String userId = createdUserId != null ? createdUserId : "b384b822-3081-701b-ea14-c492696ccc6f";

        System.out.println("Archiving user with ID: " + userId);

        String request = "{\n" +
                "    \"userId\": \"" + userId + "\",\n" +
                "    \"status\": \"ARCHIVE\"\n" +
                "}";

        // Act
        Response response = client.archiveUser(request);

        System.out.println("=== Archive User Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            System.out.println("✓ Successfully archived user");
        }
    }

    @Test(description = "Unarchive user - Success (200)", priority = 6, dependsOnMethods = "testArchiveUser_Success")
    public void testUnarchiveUser_Success() {
        // Arrange - Use the same user that was archived
        String userId = createdUserId != null ? createdUserId : "b384b822-3081-701b-ea14-c492696ccc6f";

        System.out.println("Unarchiving user with ID: " + userId);

        String request = "{\n" +
                "    \"userId\": \"" + userId + "\",\n" +
                "    \"status\": \"UNARCHIVE\"\n" +
                "}";

        // Act
        Response response = client.archiveUser(request);

        System.out.println("=== Unarchive User Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            System.out.println("✓ Successfully unarchived user");
        }
    }

    // ========== PROFILE TESTS ==========

    @Test(description = "Get profile - Success (200)")
    public void testGetProfile_Success() {
        // Act
        Response response = client.getProfile();

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            System.out.println(response.getBody().asString());
            ResponseAssertions.assertContentTypeJson(response);
            ResponseAssertions.assertJsonPathExists(response, "$.id");
            ResponseAssertions.assertJsonPathExists(response, "$.email");
            ResponseAssertions.assertJsonPathExists(response, "$.firstName");
        }
    }

    @Test(description = "Update profile - Success (200)")
    public void testUpdateProfile_Success() {
        // Arrange - Using multipart form data instead of JSON
        String firstName = "Ben";
        String lastName = "Cage";
        String preferredLanguage = "en";

        // Act - Call multipart endpoint
        Response response = client.updateProfileMultipart(firstName, lastName, preferredLanguage);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);
        System.out.println(response.getBody().asString());
        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertJsonPathExists(response, "$.firstName");
            System.out.println(response.getBody().asString());
        }
    }

    // Failed need to ask Clement why
//    @Test(description = "Update profile - Invalid data (400)")
//    public void testUpdateProfile_InvalidData() {
//        // Arrange
//
//        // Act
//        Response response = client.updateProfileMultipartInvalidData();
//
//        // Assert
//        ResponseAssertions.assertStatusCodeIn(response, 400);
//    }

    // ========== EMAIL VERIFICATION TESTS ==========
   //Failed because, returns 404 for re-verification of already verified email
    @Test(description = "Verify email - Valid token (400)")
    public void testVerifyEmail_Success() {
        userEmail = userEmail != null ? userEmail : "abc@example.com";

        // Arrange
        String request = "{\n" +
                "    \"email\": \"" + userEmail + "\"\n" +
                "}";

        // Act
        Response response = client.verifyEmail(request);

        System.out.println(response.getBody().asString());

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);
    }

//    @Test(description = "Verify email - Invalid token (403)")
//    public void testVerifyEmail_InvalidToken() {
//        userEmail = userEmail != null ? userEmail : "abc@example.com";
//
//        // Arrange
//        String request = "{\n" +
//                "    \"email\": \"papa.asante@amalitech.com\"\n" +
//                "}";
//
//        // Act
//        Response response = client.verifyEmailWithInvalidToken(request);
//
//        // Assert
//        ResponseAssertions.assertStatusCodeIn(response, 403);
//    }

    // ========== OPEN INVITE TESTS ==========

    @Test(description = "Get all open invites - Success (200)")
    public void testGetAllOpenInvites_Success() {
        // Act
        Response response = client.getAllOpenInvites();

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            ResponseAssertions.assertContentTypeJson(response);
        }
    }

     //Need valid journey ID to create open invite
    @Test(description = "Create open invite - Success (201)", priority = 5)
    public void testCreateOpenInvite_Success() {
        // Arrange
        String request = "{\n" +
                "    \"title\": \"Open Invite " + System.currentTimeMillis() + "\",\n" +
                "    \"maxUsers\": 10,\n" +
                "    \"isFirstDayRequired\": false,\n" +
                "    \"deactivateAfter\": \"NINE_MONTHS\",\n" +
                "    \"expirationDate\": \"31.12.2026\",\n" +
                "    \"journeyIds\": [\n" +
                "        \"3e888b3d-d390-4b39-ad52-a670394f8b3c\"\n" +
                "    ],\n" +
                "    \"assignedTags\": []\n" +
                "}";

        // Act
        Response response = client.createOpenInvite(request);

        // Log response for debugging
        System.out.println("=== Create Open Invite Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("===================================");

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 201);

        if (response.getStatusCode() == 201) {
            try {
                // Extract the message which may contain the invite code/ID
                String message = ResponseAssertions.extractJsonPath(response, "$.message");
                createdInviteCodeId = message;
                System.out.println("✓ Successfully created open invite. Code/ID: " + createdInviteCodeId);
            } catch (Exception e) {
                System.err.println("✗ Failed to extract invite code from response: " + e.getMessage());
            }
        } else {
            System.err.println("✗ Create open invite failed with status: " + response.getStatusCode());
        }
    }

     //Requires a valid validation code for the previous step
    @Test(description = "Verify invitation code - Valid (200)", dependsOnMethods = "testCreateOpenInvite_Success")
    public void testVerifyInvitationCode_Success() {
        // Arrange
        String code = createdInviteCodeId != null ? createdInviteCodeId : "VALID_CODE";
        String request = "{\n" +
                "    \"code\": \"" + code + "\"\n" +
                "}";

        System.out.println("Using invite code: " + code);

        // Act
        Response response = client.verifyInvitationCode(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);
    }
     //Requires a valid validation code from the create open invite test
    @Test(description = "Register with open invite - New user (200)", dependsOnMethods = "testVerifyInvitationCode_Success")
    public void testRegisterWithOpenInvite_NewUser() {
        // Arrange
        String code = createdInviteCodeId != null ? createdInviteCodeId : "INVITE_CODE";
        String request = "{\n" +
                "    \"code\": \"" + code + "\",\n" +
                "    \"firstName\": \"New\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"email\": \"newuser" + System.currentTimeMillis() + "@example.com\"\n" +
                "}";

        System.out.println("Using invite code: " + code);

        // Act
        Response response = client.registerWithOpenInviteForNewUsers(request);
        System.out.println("=== Register With Open Invite Response ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("==========================================");

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);
    }

    @Test(description = "Register with open invite - Existing user (200)", dependsOnMethods = "testRegisterWithOpenInvite_NewUser")
    public void testRegisterWithOpenInvite_ExistingUser() {
        // Arrange
        String code = createdInviteCodeId != null ? createdInviteCodeId : "INVITE_CODE";
        String request = "{\n" +
                "    \"code\": \"" + code + "\"\n" +
                "}";

        System.out.println("Using invite code: " + code);

        // Act
        Response response = client.registerWithOpenInviteForExistingUsers(request);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);
    }

    @Test(description = "Update open invite - Success (200)", priority = 6, dependsOnMethods = "testCreateOpenInvite_Success")
    public void testUpdateOpenInvite_Success() {
        // Arrange - Get the invite by code to extract its ID
        String code = createdInviteCodeId != null ? createdInviteCodeId : "sample-code-id";

        System.out.println("Looking for open invite with code: " + code);

        // Get the invite by code
        Response getByCodeResponse = client.getOpenInviteByCode(code);

        if (getByCodeResponse.getStatusCode() == 200) {
            try {
                // Extract the ID of the invite from the search results
                String jsonPath = "$.content[0].id";
                openInviteId = ResponseAssertions.extractJsonPath(getByCodeResponse, jsonPath);
                System.out.println("✓ Found open invite ID: " + openInviteId);
            } catch (Exception e) {
                System.err.println("✗ Could not find invite with code: " + code);
                System.err.println("Response: " + getByCodeResponse.getBody().asString());
            }
        }

        // Use the extracted ID or fallback
        String inviteId = openInviteId != null ? openInviteId : "sample-invite-id";

        String updateRequest = "{\n" +
                "    \"maxUses\": 20\n" +
                "}";

        System.out.println("Updating open invite with ID: " + inviteId);

        // Act
        Response response = client.updateOpenInvite(inviteId, updateRequest);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 200);

        if (response.getStatusCode() == 200) {
            System.out.println("✓ Successfully updated open invite");
        }
    }

    @Test(description = "Delete open invite - Success (204)", priority = 7, dependsOnMethods = "testUpdateOpenInvite_Success")
    public void testDeleteOpenInvite_Success() {
        // Arrange - Use the openInviteId that was extracted in the update test
        String inviteId = openInviteId != null ? openInviteId : "sample-invite-id";

        System.out.println("Deleting open invite with ID: " + inviteId);

        // Act
        Response response = client.deleteOpenInvite(inviteId);

        // Assert
        ResponseAssertions.assertStatusCodeIn(response, 204);

        if (response.getStatusCode() == 204) {
            System.out.println("✓ Successfully deleted open invite");
        }
    }
}
