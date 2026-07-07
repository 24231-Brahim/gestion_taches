package com.gestiontaches.service.mapper;

import static com.gestiontaches.domain.ActionHistoryAsserts.*;
import static com.gestiontaches.domain.ActionHistoryTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionHistoryMapperTest {

    private ActionHistoryMapper actionHistoryMapper;

    @BeforeEach
    void setUp() {
        actionHistoryMapper = new ActionHistoryMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getActionHistorySample1();
        var actual = actionHistoryMapper.toEntity(actionHistoryMapper.toDto(expected));
        assertActionHistoryAllPropertiesEquals(expected, actual);
    }
}
