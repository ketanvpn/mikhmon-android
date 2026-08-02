package com.mikhmon.android.core.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouterOsLoginProtocolTest {
    @Test
    fun authenticate_usesChallengeFromFirstDoneResponse() {
        val commands = mutableListOf<Pair<String, Map<String, String>>>()
        val challenge = "0123456789abcdef0123456789abcdef"
        val protocol = RouterOsLoginProtocol(
            username = "admin",
            password = "pässword",
            writeCommand = { command, params -> commands += command to params },
            readResponse = responsesOf(
                listOf(mapOf("!done" to "true", "ret" to challenge)),
                listOf(mapOf("!done" to "true"))
            )
        )

        val result = runBlocking { protocol.authenticate() }

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(
                "/login" to mapOf("name" to "admin", "password" to "pässword"),
                "/login" to mapOf(
                    "name" to "admin",
                    "response" to "0008bc9238e81c5b5765435ab04221a349"
                )
            ),
            commands
        )
    }

    @Test
    fun authenticate_succeedsWhenModernDoneResponseHasNoChallenge() {
        val commands = mutableListOf<Pair<String, Map<String, String>>>()
        val protocol = RouterOsLoginProtocol(
            username = "admin",
            password = "password",
            writeCommand = { command, params -> commands += command to params },
            readResponse = responsesOf(listOf(mapOf("!done" to "true")))
        )

        val result = runBlocking { protocol.authenticate() }

        assertTrue(result.isSuccess)
        assertEquals(listOf("/login" to mapOf("name" to "admin", "password" to "password")), commands)
    }

    @Test
    fun authenticate_propagatesTrapAsAuthenticationFailure() {
        val protocol = RouterOsLoginProtocol(
            username = "admin",
            password = "password",
            writeCommand = { _, _ -> },
            readResponse = responsesOf(listOf(mapOf("!trap" to "true", "message" to "invalid user name or password")))
        )

        val result = runBlocking { protocol.authenticate() }

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is AuthenticationException)
        assertEquals("invalid user name or password", result.exceptionOrNull()?.message)
    }

    private fun responsesOf(vararg responses: List<Map<String, String>>): suspend () -> List<Map<String, String>> {
        val iterator = responses.iterator()
        return { iterator.next() }
    }
}
