package com.example.backupflow.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class LocalNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val host = url.host

        if (url.scheme == "http") {
            if (!isLocalNetworkOrRfc1918(host)) {
                throw IOException("Cleartext HTTP traffic is only permitted to local RFC1918 networks and .local domains. Host: $host")
            }
        }
        return chain.proceed(request)
    }

    private fun isLocalNetworkOrRfc1918(host: String): Boolean {
        if (host.lowercase().endsWith(".local")) return true
        if (host.equals("localhost", ignoreCase = true) || host == "127.0.0.1") return true

        return try {
            val address = InetAddress.getByName(host)
            val bytes = address.address
            if (bytes.size == 4) { // IPv4
                val b0 = bytes[0].toInt() and 0xFF
                val b1 = bytes[1].toInt() and 0xFF
                (b0 == 10) || // 10.0.0.0/8
                (b0 == 172 && b1 in 16..31) || // 172.16.0.0/12
                (b0 == 192 && b1 == 168) // 192.168.0.0/16
            } else {
                false // Simplified: Not evaluating IPv6 unique local addresses right now
            }
        } catch (e: UnknownHostException) {
            false
        }
    }
}
