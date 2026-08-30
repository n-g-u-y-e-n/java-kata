package kata.advanced.rangekata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RangeTest {
  @Test
  void basicContainsTests() {

    Range<Integer> range = Range.closed(5, 7);
    assertThat(range.contains(5)).isTrue();
    assertThat(range.contains(10)).isFalse();
    assertThat(range.contains(7)).isTrue();

  }

  @Test
  void closedContainsBothEndpoints() {
    Range<Integer> range = Range.closed(5, 7);

    assertThat(range.contains(4)).isFalse();
    assertThat(range.contains(5)).isTrue();
    assertThat(range.contains(6)).isTrue();
    assertThat(range.contains(7)).isTrue();
    assertThat(range.contains(8)).isFalse();
  }

  @Test
  void openExcludesBothEndpoints() {
    Range<Integer> range = Range.open(5, 7);

    assertThat(range.contains(5)).isFalse();
    assertThat(range.contains(6)).isTrue();
    assertThat(range.contains(7)).isFalse();
  }

  @Test
  void openClosedExcludesLowerAndIncludesUpper() {
    Range<Integer> range = Range.openClosed(5, 7);

    assertThat(range.contains(5)).isFalse();
    assertThat(range.contains(6)).isTrue();
    assertThat(range.contains(7)).isTrue();
  }

  @Test
  void closedOpenIncludesLowerAndExcludesUpper() {
    Range<Integer> range = Range.closedOpen(5, 7);

    assertThat(range.contains(5)).isTrue();
    assertThat(range.contains(6)).isTrue();
    assertThat(range.contains(7)).isFalse();
  }

  @Test
  void rejectsLowerBoundGreaterThanUpperBound() {
    assertThatThrownBy(() -> Range.closed(8, 7)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void closedRangeWithEqualEndpointsContainsExactlyOneValue() {
    Range<Integer> range = Range.closed(5, 5);

    assertThat(range.contains(4)).isFalse();
    assertThat(range.contains(5)).isTrue();
    assertThat(range.contains(6)).isFalse();
  }

  @Test
  void equalEndpointsProduceEmptyRangeWhenEitherBoundIsOpen() {
    assertThat(Range.open(5, 5).contains(5)).isFalse();
    assertThat(Range.openClosed(5, 5).contains(5)).isFalse();
    assertThat(Range.closedOpen(5, 5).contains(5)).isFalse();
  }

  @Test
  void worksWithOtherComparableTypes() {
    Range<LocalDate> range =
        Range.closed(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"));

    assertThat(range.contains(LocalDate.parse("2025-12-31"))).isFalse();
    assertThat(range.contains(LocalDate.parse("2026-01-15"))).isTrue();
    assertThat(range.contains(LocalDate.parse("2026-01-31"))).isTrue();
  }

  @Test
  void unboundedRangeTests() {
    Range<Integer> lessThanFive = Range.lessThan(5); // (-Infinity, 5)
    assertThat(lessThanFive.contains(4)).isTrue();
    assertThat(lessThanFive.contains(5)).isFalse();

    Range<Integer> atMostFive = Range.atMost(5); // (-Infinity, 5]
    assertThat(atMostFive.contains(5)).isTrue();

    Range<Integer> greaterThanFive = Range.greaterThan(5); // (5, +Infinity)
    assertThat(greaterThanFive.contains(5)).isFalse();
    assertThat(greaterThanFive.contains(6)).isTrue();

    Range<Integer> atLeastFive = Range.atLeast(5); // [5, +Infinity)
    assertThat(atLeastFive.contains(5)).isTrue();

    Range<Integer> all = Range.all(); // (-Infinity, +Infinity)
    assertThat(all.contains(Integer.MIN_VALUE)).isTrue();
    assertThat(all.contains(Integer.MAX_VALUE)).isTrue();
  }

  @Test
  void enclosesRangeWithinItsBounds() {
    Range<Integer> range = Range.closed(3, 6);

    assertThat(range.encloses(Range.closed(4, 5))).isTrue();
    assertThat(range.encloses(Range.open(3, 6))).isTrue();
    assertThat(range.encloses(Range.greaterThan(3))).isFalse();
  }

  @Test
  void doesNotEncloseRangeExtendingOutsideItsBounds() {
    Range<Integer> range = Range.closed(3, 6);

    assertThat(range.encloses(Range.closedOpen(4, 7))).isFalse();
    assertThat(range.encloses(Range.openClosed(2, 5))).isFalse();
  }

  @Test
  void excludedEndpointDoesNotEncloseIncludedEndpoint() {
    assertThat(Range.open(3, 6).encloses(Range.closed(3, 5))).isFalse();
    assertThat(Range.open(3, 6).encloses(Range.closed(4, 6))).isFalse();
  }

  @Test
  void unboundedRangeEnclosesRangeWithinItsBounds() {
    assertThat(Range.atLeast(3).encloses(Range.closed(4, 100))).isTrue();
    assertThat(Range.atMost(6).encloses(Range.open(3, 6))).isTrue();
    assertThat(Range.<Integer>all().encloses(Range.closed(3, 6))).isTrue();
  }

  @Test
  void rejectsNullEndpoints() {
    assertThatThrownBy(() -> Range.closed(null, 5)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> Range.closed(5, null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> Range.lessThan(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> Range.greaterThan(null)).isInstanceOf(NullPointerException.class);
  }
}
