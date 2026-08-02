package com.mikhmon.android.data.model

data class RouterEndpoint(
    val host: String,
    val port: Int,
    val useSsl: Boolean
) {
    companion object {
        private const val API_PORT = 8728
        private const val API_SSL_PORT = 8729

        fun parse(value: String): RouterEndpoint {
            val input = value.trim()
            val (useSsl, address) = when {
                input.startsWith("api://", ignoreCase = true) -> false to input.substringAfter("://")
                input.startsWith("api-ssl://", ignoreCase = true) -> true to input.substringAfter("://")
                input.contains("://") -> throw IllegalArgumentException("Use api:// or api-ssl://, not a web URL")
                else -> false to input
            }
            require(address.isNotBlank()) { "Enter a router IP address or VPN hostname" }

            val (host, port) = parseAddress(address, if (useSsl) API_SSL_PORT else API_PORT)
            require(host.isNotBlank()) { "Enter a router IP address or VPN hostname" }
            return RouterEndpoint(host, port, useSsl)
        }

        private fun parseAddress(address: String, defaultPort: Int): Pair<String, Int> {
            if (address.startsWith("[")) {
                val closingBracket = address.indexOf(']')
                require(closingBracket > 1) { "Invalid IPv6 router address" }
                val host = address.substring(1, closingBracket)
                val suffix = address.substring(closingBracket + 1)
                val port = if (suffix.isEmpty()) defaultPort else parsePort(suffix.removePrefix(":"))
                require(suffix.isEmpty() || suffix.startsWith(":")) { "Invalid IPv6 router address" }
                return host to port
            }

            val firstColon = address.indexOf(':')
            val lastColon = address.lastIndexOf(':')
            if (firstColon != -1 && firstColon == lastColon) {
                return address.substring(0, firstColon) to parsePort(address.substring(firstColon + 1))
            }
            return address to defaultPort
        }

        private fun parsePort(value: String): Int {
            val port = value.toIntOrNull() ?: throw IllegalArgumentException("Router API port must be a number")
            require(port in 1..65535) { "Router API port must be between 1 and 65535" }
            return port
        }
    }
}
