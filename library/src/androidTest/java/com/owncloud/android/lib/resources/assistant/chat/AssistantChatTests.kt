/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.assistant.chat.model.ChatMessageRequest
import com.owncloud.android.lib.resources.assistant.v2.GetTaskTypesRemoteOperationV2
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AssistantChatTests : AbstractIT() {
    private lateinit var sessionId: String

    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_30)
        sessionId = "test-session-${System.currentTimeMillis()}"
    }

    @Test
    fun testCreateAndGetMessages() {
        val messageRequest =
            ChatMessageRequest(
                sessionId = sessionId,
                role = "human",
                content = "Hello assistant!",
                timestamp = System.currentTimeMillis()
            )

        val createResult = CreateMessageRemoteOperation(messageRequest).execute(nextcloudClient)
        assertTrue(createResult.isSuccess)

        val createdMessage = createResult.resultData!!
        assertEquals("Hello assistant!", createdMessage.content)
        assertEquals("human", createdMessage.role)
        assertEquals(sessionId.toLongOrNull(), createdMessage.sessionId)

        // Get messages for session
        val getResult = GetMessagesRemoteOperation(sessionId).execute(nextcloudClient)
        assertTrue(getResult.isSuccess)

        val messages = getResult.resultData
        assertTrue(messages.isNotEmpty())
        assertTrue(messages.any { it.id == createdMessage.id })
    }

    @Test
    fun testDeleteMessage() {
        val messageRequest =
            ChatMessageRequest(
                sessionId = sessionId,
                role = "human",
                content = "Message to delete",
                timestamp = System.currentTimeMillis()
            )
        val createResult = CreateMessageRemoteOperation(messageRequest).execute(nextcloudClient)
        assertTrue(createResult.isSuccess)

        val messageId = createResult.resultData!!.id.toString()

        // Delete the message
        val deleteResult = DeleteMessageRemoteOperation(messageId, sessionId).execute(nextcloudClient)
        assertTrue(deleteResult.isSuccess)

        // Ensure the message is gone
        val getResult = GetMessagesRemoteOperation(sessionId).execute(nextcloudClient)
        assertTrue(getResult.isSuccess)
        assertTrue(getResult.resultData!!.none { it.id.toString() == messageId })
    }

    @Test
    fun testGetAndDeleteConversations() {
        // Create a message to have a session
        val messageRequest =
            ChatMessageRequest(
                sessionId = sessionId,
                role = "human",
                content = "Starting conversation",
                timestamp = System.currentTimeMillis()
            )
        CreateMessageRemoteOperation(messageRequest).execute(nextcloudClient)

        // Get list of conversations
        val getConversationsResult = GetConversationListRemoteOperation().execute(nextcloudClient)
        assertTrue(getConversationsResult.isSuccess)

        val conversations = getConversationsResult.resultData
        assertTrue(conversations.any { it.id.toString() == sessionId })

        // Delete conversation
        val deleteResult = DeleteConversationRemoteOperation(sessionId).execute(nextcloudClient)
        assertTrue(deleteResult.isSuccess)

        // Ensure conversation is gone
        val getAfterDelete = GetConversationListRemoteOperation().execute(nextcloudClient)
        assertTrue(getAfterDelete.isSuccess)
        assertTrue(getAfterDelete.resultData!!.none { it.id.toString() == sessionId })
    }

    @Test
    fun testGetTaskTypesAndVerifyChatAndSorting() {
        val result = GetTaskTypesRemoteOperationV2().execute(nextcloudClient)

        assertTrue("Request must succeed", result.isSuccess)
        val types = result.resultData
        assertNotNull("Task types must not be null", types)
        assertTrue("Task types list must not be empty", types!!.isNotEmpty())

        val firstElementIsChat = types.first().isChat()
        assertTrue(
            "The first task type must be a chat type (sorted by isChat descending)",
            firstElementIsChat
        )

        val chatTypes = types.filter { it.isChat() }
        assertTrue("There must be at least one chat-type task", chatTypes.isNotEmpty())

        val nonChat = types.filterNot { it.isChat() }
        assertTrue(
            "There must be at least one non-chat task with single text input/output",
            nonChat.isNotEmpty()
        )

        val indexOfFirstNonChat = types.indexOfFirst { !it.isChat() }
        if (indexOfFirstNonChat > 0) {
            val anyChatAfterNonChat = types.drop(indexOfFirstNonChat).any { it.isChat() }
            assertTrue(
                "Chat types must appear before non-chat types in the list",
                !anyChatAfterNonChat
            )
        }

        types.forEach { tt ->
            assertNotNull("Each task type must have an ID assigned", tt.id)
        }
    }
}
