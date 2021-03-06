package com.cherryperry.amiami.controller

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@RunWith(SpringJUnit4ClassRunner::class)
class CurrencyControllerTest {

    private val itemRepository = CurrencyControllerCurrencyRepositoryTestImpl()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(CurrencyController(itemRepository))
        .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
        .build()

    private val validLastModifiedString = DateTimeFormatter.RFC_1123_DATE_TIME
        .withZone(ZoneId.of("UTC"))
        .format(Instant.ofEpochMilli(itemRepository.lastModified))

    private val oldLastModifiedString = DateTimeFormatter.RFC_1123_DATE_TIME
        .withZone(ZoneId.of("UTC"))
        .format(Instant.ofEpochMilli(itemRepository.lastModified - TimeUnit.DAYS.toMillis(1)))

    @Test
    fun testDefaultResponse() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/currency"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LAST_MODIFIED, validLastModifiedString))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.header().string(
                HttpHeaders.CACHE_CONTROL, "max-age=0, private, stale-if-error=604800"))
            .andExpect(MockMvcResultMatchers.content().string("{\"success\":true,\"rates\":{\"USD\":1.0,\"EUR\":1.0}}"))
    }

    @Test
    fun testNotModifiedHeader() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/currency")
            .header(HttpHeaders.IF_MODIFIED_SINCE, validLastModifiedString))
            .andExpect(MockMvcResultMatchers.status().isNotModified)
    }

    @Test
    fun testOldHeader() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/currency")
            .header(HttpHeaders.IF_MODIFIED_SINCE, oldLastModifiedString))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.header().string(
                HttpHeaders.CACHE_CONTROL, "max-age=0, private, stale-if-error=604800"))
    }
}
