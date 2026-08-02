package com.mikhmon.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouterEndpointTest {
    @Test
    fun parse_usesPlainApiDefaultsForLocalIp() {
        val endpoint = RouterEndpoint.parse("192.168.88.1")

        assertEquals("192.168.88.1", endpoint.host)
        assertEquals(8728, endpoint.port)
        assertFalse(endpoint.useSsl)
    }

    @Test
    fun parse_supportsVpnHostnameAndExplicitPort() {
        val endpoint = RouterEndpoint.parse("vpn-router.example.com:18728")

        assertEquals("vpn-router.example.com", endpoint.host)
        assertEquals(18728, endpoint.port)
        assertFalse(endpoint.useSsl)
    }

    @Test
    fun parse_usesTlsForApiSslUrl() {
        val endpoint = RouterEndpoint.parse("api-ssl://[fd00::1]")

        assertEquals("fd00::1", endpoint.host)
        assertEquals(8729, endpoint.port)
        assertTrue(endpoint.useSsl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_rejectsWebUrls() {
        RouterEndpoint.parse("https://192.168.88.1")
    }
}
