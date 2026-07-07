package com.gestiontaches.service.mapper;

import static com.gestiontaches.domain.IssueAsserts.*;
import static com.gestiontaches.domain.IssueTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IssueMapperTest {

    private IssueMapper issueMapper;

    @BeforeEach
    void setUp() {
        issueMapper = new IssueMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getIssueSample1();
        var actual = issueMapper.toEntity(issueMapper.toDto(expected));
        assertIssueAllPropertiesEquals(expected, actual);
    }
}
