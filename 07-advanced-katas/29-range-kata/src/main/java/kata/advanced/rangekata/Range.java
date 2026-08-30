package kata.advanced.rangekata;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// Range defines the boundaries around a contiguous span of values of some {@link Comparable} type.
public final class Range<C extends Comparable<? super C>> {

    @Nullable
    private final C lowerBound;
    @Nullable
    private final C upperBound;
    @Nullable
    private final BoundType lowerBoundType;
    @Nullable
    private final BoundType upperBoundType;

    private Range(BoundType lowerBoundType, C lowerBound, C upperBound, BoundType upperBoundType) {
        if (lowerBound != null && upperBound != null && lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("bad input");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerBoundType = lowerBoundType;
        this.upperBoundType = upperBoundType;
    }

    public static <T extends Comparable<? super T>> Range<T> closed(T lowerBound, T upperBound) {
        return new Range<T>(BoundType.INCLUSIVE, Objects.requireNonNull(lowerBound),
                Objects.requireNonNull(upperBound), BoundType.INCLUSIVE);
    }

    public static <T extends Comparable<? super T>> Range<T> open(T lowerBound, T upperBound) {
        return new Range<T>(BoundType.EXCLUSIVE, Objects.requireNonNull(lowerBound),
                Objects.requireNonNull(upperBound), BoundType.EXCLUSIVE);
    }

    public static <T extends Comparable<? super T>> Range<T> openClosed(T lowerBound,
            T upperBound) {
        return new Range<T>(BoundType.EXCLUSIVE, Objects.requireNonNull(lowerBound),
                Objects.requireNonNull(upperBound), BoundType.INCLUSIVE);
    }

    public static <T extends Comparable<? super T>> Range<T> closedOpen(T lowerBound,
            T upperBound) {
        return new Range<T>(BoundType.INCLUSIVE, Objects.requireNonNull(lowerBound),
                Objects.requireNonNull(upperBound), BoundType.EXCLUSIVE);
    }

    public static <T extends Comparable<? super T>> Range<T> lessThan(T upperBound) {
        return new Range<T>(null, null, Objects.requireNonNull(upperBound), BoundType.EXCLUSIVE);
    }

    public static <T extends Comparable<? super T>> Range<T> atMost(T upperBound) {
        return new Range<T>(null, null, Objects.requireNonNull(upperBound), BoundType.INCLUSIVE);
    }

    public static <T extends Comparable<? super T>> Range<T> greaterThan(T lowerBound) {
        return new Range<T>(BoundType.EXCLUSIVE, Objects.requireNonNull(lowerBound), null, null);
    }

    public static <T extends Comparable<? super T>> Range<T> atLeast(T lowerBound) {
        return new Range<T>(BoundType.INCLUSIVE, Objects.requireNonNull(lowerBound), null, null);
    }

    public static <T extends Comparable<? super T>> Range<T> all() {
        return new Range<T>(null, null, null, null);
    }


    public boolean contains(C value) {
        boolean aboveLower = lowerBound == null
                || lowerBoundType.isWithinLowerBound(lowerBound.compareTo(value));

        boolean belowUpper = upperBound == null
                || upperBoundType.isWithinUpperBound(upperBound.compareTo(value));

        return aboveLower && belowUpper;
    }

    public boolean encloses(Range<C> anotherRange) {
        if (!anotherRange.isBounded()) return false;
        return this.contains(anotherRange.lowerBound) && this.contains(anotherRange.upperBound);
    }

    private boolean isBounded() {
        return this.lowerBound != null && this.upperBound != null;
    }

    private enum BoundType {

        INCLUSIVE {
            @Override
            boolean isWithinLowerBound(int comparison) {
                return comparison <= 0;
            }

            @Override
            boolean isWithinUpperBound(int comparison) {
                return comparison >= 0;
            }

        },
        EXCLUSIVE {
            @Override
            boolean isWithinLowerBound(int comparison) {
                return comparison < 0;
            }

            @Override
            boolean isWithinUpperBound(int comparison) {
                return comparison > 0;
            }
        };

        abstract boolean isWithinLowerBound(int comparison);

        abstract boolean isWithinUpperBound(int comparison);
    }
}
