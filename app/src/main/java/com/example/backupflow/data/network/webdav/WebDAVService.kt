package com.example.backupflow.data.network.webdav

import com.example.backupflow.data.network.LocalNetworkInterceptor
import com.example.backupflow.security.WebDAVCertificateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class WebDAVService @Inject constructor(
    private val certificateStore: WebDAVCertificateStore
) {
    private var client: OkHttpClient? = null
    private var baseUrl: String = ""
    private var authHeader: String? = null

    fun connect(host: String, port: Int, username: String, password: String?, isHttps: Boolean = true) {
        val scheme = if (isHttps) "https" else "http"
        baseUrl = "$scheme://$host:$port"

        if (username.isNotEmpty() && !password.isNullOrEmpty()) {
            authHeader = Credentials.basic(username, password)
        }

        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                if (!chain.isNullOrEmpty()) {
                    val leaf = chain[0]
                    certificateStore.validate(leaf, host, port)
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

        client = OkHttpClient.Builder()
            .addInterceptor(LocalNetworkInterceptor())
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true } // Pinned via TOFU in TrustManager
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        client?.dispatcher?.executorService?.shutdown()
        client?.connectionPool?.evictAll()
        client = null
        authHeader = null
    }

    @Throws(IOException::class)
    suspend fun ensureDirectoryExists(directoryPath: String) = withContext(Dispatchers.IO) {
        val okHttpClient = client ?: throw IOException("WebDAV not connected")
        val parts = directoryPath.split("/").filter { it.isNotEmpty() }
        var currentPath = ""

        for (part in parts) {
            currentPath = "$currentPath/$part"
            val url = "$baseUrl$currentPath"

            // Send MKCOL
            val requestBuilder = Request.Builder()
                .url(url)
                .method("MKCOL", null)

            authHeader?.let { requestBuilder.header("Authorization", it) }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            response.use {
                if (!it.isSuccessful && it.code != 405) { // 405 Method Not Allowed usually means folder already exists
                    throw IOException("WebDAV MKCOL failed for $url with code ${it.code}")
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun uploadFile(remotePath: String, inputStream: InputStream) = withContext(Dispatchers.IO) {
        val okHttpClient = client ?: throw IOException("WebDAV not connected")
        val lastSlash = remotePath.lastIndexOf('/')
        val dir = if (lastSlash > 0) remotePath.substring(0, lastSlash) else ""
        val fileName = remotePath.substring(lastSlash + 1)
        val tempFileName = "$fileName.tmp"

        if (dir.isNotEmpty()) {
            ensureDirectoryExists(dir)
        }

        val tempUrl = "$baseUrl/$dir/$tempFileName".replace("//", "/")
        val finalUrl = "$baseUrl/$dir/$fileName".replace("//", "/")

        val requestBody = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()

            override fun writeTo(sink: BufferedSink) {
                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    sink.write(buffer, 0, bytesRead)
                }
            }
        }

        // 1. PUT to .tmp
        val putRequestBuilder = Request.Builder()
            .url(tempUrl)
            .put(requestBody)

        authHeader?.let { putRequestBuilder.header("Authorization", it) }

        val putResponse = okHttpClient.newCall(putRequestBuilder.build()).execute()
        putResponse.use {
            if (!it.isSuccessful) {
                throw IOException("WebDAV PUT failed for $tempUrl with code ${it.code}")
            }
        }

        // 2. MOVE from .tmp to finalUrl
        val moveRequestBuilder = Request.Builder()
            .url(tempUrl)
            .method("MOVE", null)
            .header("Destination", finalUrl)
            .header("Overwrite", "T")

        authHeader?.let { moveRequestBuilder.header("Authorization", it) }

        val moveResponse = okHttpClient.newCall(moveRequestBuilder.build()).execute()
        moveResponse.use {
            if (!it.isSuccessful) {
                throw IOException("WebDAV MOVE failed from $tempUrl to $finalUrl with code ${it.code}")
            }
        }
    }

    @Throws(IOException::class)
    suspend fun deleteFile(remotePath: String) = withContext(Dispatchers.IO) {
        val okHttpClient = client ?: throw IOException("WebDAV not connected")
        val url = "$baseUrl/$remotePath".replace("//", "/")

        val requestBuilder = Request.Builder()
            .url(url)
            .delete()

        authHeader?.let { requestBuilder.header("Authorization", it) }

        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        response.use {
            if (!it.isSuccessful && it.code != 404) {
                throw IOException("WebDAV DELETE failed for $url with code ${it.code}")
            }
        }
    }
}
