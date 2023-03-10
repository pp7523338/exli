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

public class UserTaskListsBase extends Resource {

    /**
     * @param client Parent client instance
     */
    public UserTaskListsBase(Client client) {
        super(client);
    }

    /**
     * Get a user task list
     * Returns the full record for a user task list.
     * @param userTaskListGid Globally unique identifier for the user task list. (required)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return ItemRequest(UserTaskList)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ItemRequest<UserTaskList> getUserTaskList(String userTaskListGid, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/user_task_lists/{user_task_list_gid}".replace("{user_task_list_gid}", userTaskListGid);
        new Here("Randoop", 31).given(userTaskListGid, "https://app.asana.com/-/oauth_authorize?client_id&redirect_uri=hi!&response_type=code&state=Server%20Error").checkEq(path, "/user_task_lists/https://app.asana.com/-/oauth_authorize?client_id&redirect_uri=hi!&response_type=code&state=Server%20Error");
        new Here("Randoop", 31).given(userTaskListGid, "Sync token invalid or too old").checkEq(path, "/user_task_lists/Sync token invalid or too old");
        new Here("Randoop", 31).given(userTaskListGid, "/tasks/Not Found/removeProject").checkEq(path, "/user_task_lists//tasks/Not Found/removeProject");
        new Here("Randoop", 31).given(userTaskListGid, "GET").checkEq(path, "/user_task_lists/GET");
        ItemRequest<UserTaskList> req = new ItemRequest<UserTaskList>(this, UserTaskList.class, path, "GET").query("opt_pretty", optPretty).query("opt_fields", optFields);
        return req;
    }

    public ItemRequest<UserTaskList> getUserTaskList(String userTaskListGid) throws IOException {
        return getUserTaskList(userTaskListGid, null, false);
    }

    /**
     * Get a user&#x27;s task list
     * Returns the full record for a user&#x27;s task list.
     * @param userGid A string identifying a user. This can either be the string \&quot;me\&quot;, an email, or the gid of a user. (required)
     * @param workspace The workspace in which to get the user task list. (required)
     * @param optFields Defines fields to return. Some requests return *compact* representations of objects in order to conserve resources and complete the request more efficiently. Other times requests return more information than you may need. This option allows you to list the exact set of fields that the API should be sure to return for the objects. The field names should be provided as paths, described below. The id of included objects will always be returned, regardless of the field options. (optional)
     * @param optPretty Provides “pretty” output. Provides the response in a “pretty” format. In the case of JSON this means doing proper line breaking and indentation to make it readable. This will take extra time and increase the response size so it is advisable only to use this during debugging. (optional)
     * @return ItemRequest(UserTaskList)
     * @throws IOException If we fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ItemRequest<UserTaskList> getUserTaskListForUser(String userGid, String workspace, List<String> optFields, Boolean optPretty) throws IOException {
        String path = "/users/{user_gid}/user_task_list".replace("{user_gid}", userGid);
        new Here("Randoop", 54).given(userGid, "Sync token invalid or too old").checkEq(path, "/users/Sync token invalid or too old/user_task_list");
        new Here("Randoop", 54).given(userGid, "hi!").checkEq(path, "/users/hi!/user_task_list");
        new Here("Randoop", 54).given(userGid, "com.asana.errors.InvalidRequestError: Invalid Request").checkEq(path, "/users/com.asana.errors.InvalidRequestError: Invalid Request/user_task_list");
        new Here("Randoop", 54).given(userGid, "POST").checkEq(path, "/users/POST/user_task_list");
        new Here("Randoop", 54).given(userGid, "Not Found").checkEq(path, "/users/Not Found/user_task_list");
        ItemRequest<UserTaskList> req = new ItemRequest<UserTaskList>(this, UserTaskList.class, path, "GET").query("opt_pretty", optPretty).query("opt_fields", optFields).query("workspace", workspace);
        return req;
    }

    public ItemRequest<UserTaskList> getUserTaskListForUser(String userGid, String workspace) throws IOException {
        return getUserTaskListForUser(userGid, workspace, null, false);
    }
}
