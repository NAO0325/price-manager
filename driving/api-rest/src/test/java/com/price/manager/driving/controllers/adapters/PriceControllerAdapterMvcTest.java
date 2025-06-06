package com.price.manager.driving.controllers.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.domain.Price;
import com.price.manager.driving.controllers.error.CustomExceptionHandler;
import com.price.manager.driving.controllers.mappers.PriceMapper;
import com.price.manager.driving.controllers.utils.PriceMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceControllerAdapter.class)
@ContextConfiguration(classes = {PriceControllerAdapter.class, CustomExceptionHandler.class})
class PriceControllerAdapterMvcTest {

    private PriceMocks mocks;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PriceMapper priceMapper;

    @MockBean
    private PriceServicePort priceServicePort;

    @BeforeEach
    void setup() {
        this.mocks = new PriceMocks();
    }

    @Test
    void whenValidInputTest1ThenReturns200() throws Exception {
        // Given
        when(this.priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any()))
                .thenReturn(Price.builder().build());
        when(this.priceMapper.toResponseDto(any()))
                .thenReturn(this.mocks.createPriceResponse1());

        // When & Then
        this.mvc.perform(get("/v1/price/findByBrandProductBetweenDate")
                        .param("dateQuery", "2020-06-14T10:00:00Z")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T00:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59Z"));
    }

    @Test
    void whenValidInputTest2ThenReturns200() throws Exception {
        // Given
        when(this.priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any()))
                .thenReturn(Price.builder().build());
        when(this.priceMapper.toResponseDto(any()))
                .thenReturn(this.mocks.createPriceResponse2());

        // When & Then
        this.mvc.perform(get("/v1/price/findByBrandProductBetweenDate")
                        .param("dateQuery", "2020-06-14T16:00:00Z")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.price").value(25.45))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T15:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2020-06-14T18:30:00Z"));
    }

    @Test
    void whenValidInputTest3ThenReturns200() throws Exception {
        // Given
        when(this.priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any()))
                .thenReturn(Price.builder().build());
        when(this.priceMapper.toResponseDto(any()))
                .thenReturn(this.mocks.createPriceResponse3());

        // When & Then
        this.mvc.perform(get("/v1/price/findByBrandProductBetweenDate")
                        .param("dateQuery", "2020-06-14T21:00:00Z")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.price").value(30.5))
                .andExpect(jsonPath("$.startDate").value("2020-06-15T00:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2020-06-15T11:00:00Z"));
    }

    @Test
    void whenValidInputTest4ThenReturns200() throws Exception {
        // Given
        when(this.priceServicePort.findByBrandProductBetweenDate(anyLong(), anyLong(), any()))
                .thenReturn(Price.builder().build());
        when(this.priceMapper.toResponseDto(any()))
                .thenReturn(this.mocks.createPriceResponse4());

        // When & Then
        this.mvc.perform(get("/v1/price/findByBrandProductBetweenDate")
                        .param("dateQuery", "2020-06-16T21:00:00Z")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.price").value(38.95))
                .andExpect(jsonPath("$.startDate").value("2020-06-15T16:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59Z"));
    }


    @Test
    void whenBadRequestThenReturns400() throws Exception {
        // When & Then
        this.mvc.perform(get("/v1/price/findByBrandProductBetweenDate")
                        .param("dateQuery", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value(containsString("Parameter 'dateQuery' must be valid")))
                .andExpect(jsonPath("$.message").value(containsString("Failed to convert value")))
                .andExpect(jsonPath("$.message").value(containsString("2020-06-14T10:00:00")));
    }

    @Test
    void whenPriceNotFoundThenReturns404() throws Exception {
        // When & Then
        this.mvc.perform(get("/v1/price/findByBrandProductBetweenDate")
                        .param("dateQuery", "2020-06-14T10:00:00Z")
                        .param("productId", "35455")
                        .param("brandId", "8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRICE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No price found for the given parameters"));
    }

    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return Jackson2ObjectMapperBuilder.json()
                    .modules(new JavaTimeModule())
                    .build();
        }
    }
}

