package com.asana.resources.gen;

import com.asana.Client;
import com.asana.resources.Resource;
import com.asana.requests.ItemRequest;
import com.asana.requests.CollectionRequest;
import com.asana.models.*;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.inlinetest.Here;
import static org.inlinetest.Here.group;

public class UsersBase extends Resource {

    /**
     * @param client Parent client instance
     */
    public UsersBase(Client client) {
        super(client);
    }

    /**
     * Get a user&#x27;s favorites
     * Returns all of a user&#x27;s favorites in the given workspace, of the given type. Results are given in order (The same order as Asana&#x27;s sidebar).
     * @param userGid A string identifying a user. This can either be the string \&quot;me\&quot;, an email, or the gid of a user. (required)
     * @param workspace The workspace in which to get favorites. (required)
     * @param resourceType The resource type of favorites to be returned. (required)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return CollectionRequest(JsonElement)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public CollectionRequest<JsonElement> getFavoritesForUser(String userGid, String workspace, String resourceType, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/users/{user_gid}/favorites".replace("{user_gid}", userGid);
        new Here("Randoop", 33).given(userGid, "").checkEq(path, "/users//favorites");
        new Here("Randoop", 33).given(userGid, "GET").checkEq(path, "/users/GET/favorites");
        new Here("Randoop", 33).given(userGid, "https://app.asana.com/-/oauth_authorize?client_id&redirect_uri=hi!&response_type=code&state=com.asana.errors.RateLimitEnforcedError:%20Rate%20Limit%20Enforced").checkEq(path, "/users/https://app.asana.com/-/oauth_authorize?client_id&redirect_uri=hi!&response_type=code&state=com.asana.errors.RateLimitEnforcedError:%20Rate%20Limit%20Enforced/favorites");
        new Here("Randoop", 33).given(userGid, "200").checkEq(path, "/users/200/favorites");
        new Here("Randoop", 33).given(userGid, "NoAuthorizationError").checkEq(path, "/users/NoAuthorizationError/favorites");
        new Here("Randoop", 33).given(userGid, "com.asana.errors.RetryableAsanaError: Not Found").checkEq(path, "/users/com.asana.errors.RetryableAsanaError: Not Found/favorites");
        new Here("Randoop", 33).given(userGid, "Forbidden").checkEq(path, "/users/Forbidden/favorites");
        new Here("Randoop", 33).given(userGid, "Not Found").checkEq(path, "/users/Not Found/favorites");
        CollectionRequest<JsonElement> req = new CollectionRequest<JsonElement>(this, JsonElement.class, path, "GET").query("opt_pretty", optPretty).query("opt_fields", optFields).query("resource_type", resourceType).query("workspace", workspace);
        return req;
    }

    public CollectionRequest<JsonElement> getFavoritesForUser(String userGid, String workspace, String resourceType) throws IOException {
        return getFavoritesForUser(userGid, workspace, resourceType, null, false);
    }

    /**
     * Get a user
     * Returns the full user record for the single user with the provided ID.
     * @param userGid A string identifying a user. This can either be the string \&quot;me\&quot;, an email, or the gid of a user. (required)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return ItemRequest(User)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ItemRequest<User> getUser(String userGid, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/users/{user_gid}".replace("{user_gid}", userGid);
        new Here("Randoop", 57).given(userGid, "https://app.asana.com/-/oauth_authorize?client_id&redirect_uri=hi!&response_type=code&state=404").checkEq(path, "/users/https://app.asana.com/-/oauth_authorize?client_id&redirect_uri=hi!&response_type=code&state=404");
        new Here("Randoop", 57).given(userGid, "com.asana.errors.RateLimitEnforcedError: Rate Limit Enforced").checkEq(path, "/users/com.asana.errors.RateLimitEnforcedError: Rate Limit Enforced");
        new Here("Randoop", 57).given(userGid, "GET").checkEq(path, "/users/GET");
        new Here("Randoop", 57).given(userGid, "Sync token invalid or too old").checkEq(path, "/users/Sync token invalid or too old");
        ItemRequest<User> req = new ItemRequest<User>(this, User.class, path, "GET").query("opt_pretty", optPretty).query("opt_fields", optFields);
        return req;
    }

    public ItemRequest<User> getUser(String userGid) throws IOException {
        return getUser(userGid, null, false);
    }

    /**
     * Get multiple users
     * Returns the user records for all users in all workspaces and organizations accessible to the authenticated user. Accepts an optional workspace ID parameter. Results are sorted by user ID.
     * @param team The team ID to filter users on. (optional)
     * @param workspace The workspace or organization ID to filter users on. (optional)
     * @param offset Offset token. An offset to the next page returned by the API. A pagination request will return an offset token, which can be used as an input parameter to the next request. If an offset is not passed in, the API will return the first page of results. &#x27;Note: You can only pass in an offset that was returned to you via a previously paginated request.&#x27; (optional)
     * @param limit Results per page. The number of objects to return per page. The value must be between 1 and 100. (optional)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return CollectionRequest(User)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public CollectionRequest<User> getUsers(String team, String workspace, String offset, Integer limit, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/users";
        CollectionRequest<User> req = new CollectionRequest<User>(this, User.class, path, "GET").query("workspace", workspace).query("team", team).query("opt_pretty", optPretty).query("opt_fields", optFields).query("limit", limit).query("offset", offset);
        return req;
    }

    public CollectionRequest<User> getUsers(String team, String workspace) throws IOException {
        return getUsers(team, workspace, null, (int) Client.DEFAULTS.get("page_size"), null, false);
    }

    /**
     * Get users in a team
     * Returns the compact records for all users that are members of the team. Results are sorted alphabetically and limited to 2000. For more results use the &#x60;/users&#x60; endpoint.
     * @param teamGid Globally unique identifier for the team. (required)
     * @param offset Offset token. An offset to the next page returned by the API. A pagination request will return an offset token, which can be used as an input parameter to the next request. If an offset is not passed in, the API will return the first page of results. &#x27;Note: You can only pass in an offset that was returned to you via a previously paginated request.&#x27; (optional)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return CollectionRequest(User)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public CollectionRequest<User> getUsersForTeam(String teamGid, String offset, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/teams/{team_gid}/users".replace("{team_gid}", teamGid);
        new Here("Randoop", 109).given(teamGid, "hi!").checkEq(path, "/teams/hi!/users");
        new Here("Randoop", 109).given(teamGid, "urn:ietf:wg:oauth:2.0:oob").checkEq(path, "/teams/urn:ietf:wg:oauth:2.0:oob/users");
        new Here("Randoop", 109).given(teamGid, "").checkEq(path, "/teams//users");
        CollectionRequest<User> req = new CollectionRequest<User>(this, User.class, path, "GET").query("opt_pretty", optPretty).query("opt_fields", optFields).query("offset", offset);
        return req;
    }

    public CollectionRequest<User> getUsersForTeam(String teamGid) throws IOException {
        return getUsersForTeam(teamGid, null, null, false);
    }

    /**
     * Get users in a workspace or organization
     * Returns the compact records for all users in the specified workspace or organization. Results are sorted alphabetically and limited to 2000. For more results use the &#x60;/users&#x60; endpoint.
     * @param workspaceGid Globally unique identifier for the workspace or organization. (required)
     * @param offset Offset token. An offset to the next page returned by the API. A pagination request will return an offset token, which can be used as an input parameter to the next request. If an offset is not passed in, the API will return the first page of results. &#x27;Note: You can only pass in an offset that was returned to you via a previously paginated request.&#x27; (optional)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return CollectionRequest(User)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public CollectionRequest<User> getUsersForWorkspace(String workspaceGid, String offset, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/workspaces/{workspace_gid}/users".replace("{workspace_gid}", workspaceGid);
        new Here("Randoop", 133).given(workspaceGid, "Not Found").checkEq(path, "/workspaces/Not Found/users");
        CollectionRequest<User> req = new CollectionRequest<User>(this, User.class, path, "GET").query("opt_pretty", optPretty).query("opt_fields", optFields).query("offset", offset);
        return req;
    }

    public CollectionRequest<User> getUsersForWorkspace(String workspaceGid) throws IOException {
        return getUsersForWorkspace(workspaceGid, null, null, false);
    }
}
