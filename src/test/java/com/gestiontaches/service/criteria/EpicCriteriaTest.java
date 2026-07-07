package com.gestiontaches.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class EpicCriteriaTest {

    @Test
    void newEpicCriteriaHasAllFiltersNullTest() {
        var epicCriteria = new EpicCriteria();
        assertThat(epicCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void epicCriteriaFluentMethodsCreatesFiltersTest() {
        var epicCriteria = new EpicCriteria();

        setAllFilters(epicCriteria);

        assertThat(epicCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void epicCriteriaCopyCreatesNullFilterTest() {
        var epicCriteria = new EpicCriteria();
        var copy = epicCriteria.copy();

        assertThat(epicCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(epicCriteria)
        );
    }

    @Test
    void epicCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var epicCriteria = new EpicCriteria();
        setAllFilters(epicCriteria);

        var copy = epicCriteria.copy();

        assertThat(epicCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(epicCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var epicCriteria = new EpicCriteria();

        assertThat(epicCriteria).hasToString("EpicCriteria{}");
    }

    private static void setAllFilters(EpicCriteria epicCriteria) {
        epicCriteria.id();
        epicCriteria.title();
        epicCriteria.description();
        epicCriteria.status();
        epicCriteria.priority();
        epicCriteria.createdAt();
        epicCriteria.updatedAt();
        epicCriteria.projectId();
        epicCriteria.distinct();
    }

    private static Condition<EpicCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitle()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getPriority()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getProjectId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<EpicCriteria> copyFiltersAre(EpicCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitle(), copy.getTitle()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getPriority(), copy.getPriority()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getProjectId(), copy.getProjectId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
