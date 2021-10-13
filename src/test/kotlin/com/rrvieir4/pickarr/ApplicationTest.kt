package com.rrvieir4.pickarr

import com.rrvieir4.pickarr.plugins.configureRouting
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}