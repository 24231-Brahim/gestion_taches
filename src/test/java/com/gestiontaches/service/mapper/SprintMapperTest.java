package com.gestiontaches.service.mapper;

import static com.gestiontaches.domain.SprintAsserts.*;
import static com.gestiontaches.domain.SprintTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SprintMapperTest {

    private SprintMapper sprintMapper;

    @BeforeEach
    void setUp() {
        sprintMapper = new SprintMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSprintSample1();
        var actual = sprintMapper.toEntity(sprintMapper.toDto(expected));
        assertSprintAllPropertiesEquals(expected, actual);
    }
}
