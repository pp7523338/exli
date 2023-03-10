package com.asana.resources;

import com.asana.Client;
import com.asana.models.UserTaskList;
import com.asana.requests.CollectionRequest;
import com.asana.requests.ItemRequest;
import com.asana.resources.gen.UserTaskListsBase;
import org.inlinetest.Here;
import static org.inlinetest.Here.group;

public class UserTaskLists extends UserTaskListsBase {

    public UserTaskLists(Client client) {
        super(client);
    }

    /**
     * Returns the full record for the user task list for the given user
     *
     * @param  user An identifier for the user. Can be one of an email address,
     * the globally unique identifier for the user, or the keyword `me`
     * to indicate the current user making the request.
     * @return Request object
     */
    public ItemRequest<UserTaskList> findByUser(String user) {
        String path = String.format("/users/%s/user_task_list", user);
        new Here("Randoop", 24).given(user, "Sync token invalid or too old").checkEq(path, "/users/Sync token invalid or too old/user_task_list");
        return new ItemRequest<UserTaskList>(this, UserTaskList.class, path, "GET");
    }

    /**
     * Returns the full record for a user task list.
     *
     * @param  userTaskList Globally unique identifier for the user task list.
     * @return Request object
     */
    public ItemRequest<UserTaskList> findById(String userTaskList) {
        String path = String.format("/user_task_lists/%s", userTaskList);
        new Here("Unit", 36).given(userTaskList, "1").checkEq(path, "/user_task_lists/1");
        return new ItemRequest<UserTaskList>(this, UserTaskList.class, path, "GET");
    }

    /**
     * Returns the compact list of tasks in a user's My Tasks list. The returned
     * tasks will be in order within each assignee status group of `Inbox`,
     * `Today`, and `Upcoming`.
     *
     * **Note:** tasks in `Later` have a different ordering in the Asana web app
     * than the other assignee status groups; this endpoint will still return
     * them in list order in `Later` (differently than they show up in Asana,
     * but the same order as in Asana's mobile apps).
     *
     * **Note:** Access control is enforced for this endpoint as with all Asana
     * API endpoints, meaning a user's private tasks will be filtered out if the
     * API-authenticated user does not have access to them.
     *
     * **Note:** Both complete and incomplete tasks are returned by default
     * unless they are filtered out (for example, setting `completed_since=now`
     * will return only incomplete tasks, which is the default view for "My
     * Tasks" in Asana.)
     *
     * @param  userTaskList The user task list in which to search for tasks.
     * @return Request object
     */
    public CollectionRequest<UserTaskList> tasks(String userTaskList) {
        String path = String.format("/user_task_lists/%s/tasks", userTaskList);
        new Here("Randoop", 64).given(userTaskList, "hi!").checkEq(path, "/user_task_lists/hi!/tasks");
        return new CollectionRequest<UserTaskList>(this, UserTaskList.class, path, "GET");
    }
}
