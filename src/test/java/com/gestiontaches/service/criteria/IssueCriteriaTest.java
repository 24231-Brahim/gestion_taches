package com.gestiontaches.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class IssueCriteriaTest {

    @Test
    void newIssueCriteriaHasAllFiltersNullTest() {
        var issueCriteria = new IssueCriteria();
        assertThat(issueCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void issueCriteriaFluentMethodsCreatesFiltersTest() {
        var issueCriteria = new IssueCriteria();

        setAllFilters(issueCriteria);

        assertThat(issueCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void issueCriteriaCopyCreatesNullFilterTest() {
        var issueCriteria = new IssueCriteria();
        var copy = issueCriteria.copy();

        assertThat(issueCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(issueCriteria)
        );
    }

    @Test
    void issueCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var issueCriteria = new IssueCriteria();
        setAllFilters(issueCriteria);

        var copy = issueCriteria.copy();

        assertThat(issueCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(issueCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var issueCriteria = new IssueCriteria();

        assertThat(issueCriteria).hasToString("IssueCriteria{}");
    }

    private static void setAllFilters(IssueCriteria issueCriteria) {
        issueCriteria.id();
        issueCriteria.title();
        issueCriteria.description();
        issueCriteria.type();
        issueCriteria.status();
        issueCriteria.priority();
        issueCriteria.createdAt();
        issueCriteria.updatedAt();
        issueCriteria.commentsId();
        issueCriteria.attachmentsId();
        issueCriteria.historyId();
        issueCriteria.sprintId();
        issueCriteria.epicId();
        issueCriteria.projectId();
        issueCriteria.distinct();
    }

    private static Condition<IssueCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitle()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getPriority()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getCommentsId()) &&
                condition.apply(criteria.getAttachmentsId()) &&
                condition.apply(criteria.getHistoryId()) &&
                condition.apply(criteria.getSprintId()) &&
                condition.apply(criteria.getEpicId()) &&
                condition.apply(criteria.getProjectId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<IssueCriteria> copyFiltersAre(IssueCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitle(), copy.getTitle()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getPriority(), copy.getPriority()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getCommentsId(), copy.getCommentsId()) &&
                condition.apply(criteria.getAttachmentsId(), copy.getAttachmentsId()) &&
                condition.apply(criteria.getHistoryId(), copy.getHistoryId()) &&
                condition.apply(criteria.getSprintId(), copy.getSprintId()) &&
                condition.apply(criteria.getEpicId(), copy.getEpicId()) &&
                condition.apply(criteria.getProjectId(), copy.getProjectId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
