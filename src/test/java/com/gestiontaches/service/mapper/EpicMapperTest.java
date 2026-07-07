package com.gestiontaches.service.mapper;

import static com.gestiontaches.domain.EpicAsserts.*;
import static com.gestiontaches.domain.EpicTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EpicMapperTest {

    private EpicMapper epicMapper;

    @BeforeEach
    void setUp() {
        epicMapper = new EpicMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEpicSample1();
        var actual = epicMapper.toEntity(epicMapper.toDto(expected));
        assertEpicAllPropertiesEquals(expected, actual);
    }
}
