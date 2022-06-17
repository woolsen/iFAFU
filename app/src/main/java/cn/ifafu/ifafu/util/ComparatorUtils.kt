package cn.ifafu.ifafu.util

import java.util.*

object ComparatorUtils {

    /**
     * Returns a comparator that imposes the reverse ordering of this
     * comparator.
     *
     * @return a comparator that imposes the reverse ordering of this
     *         comparator.
     */
    fun <T> Comparator<T>.reverse(): Comparator<T> {
        return Collections.reverseOrder(this)
    }

    /**
     * Returns a lexicographic-order comparator with another comparator.
     * If this {@code Comparator} considers two elements equal, i.e.
     * {@code compare(a, b) == 0}, {@code other} is used to determine the order.
     *
     * <p>The returned comparator is serializable if the specified comparator
     * is also serializable.
     *
     * @apiNote
     * For example, to sort a collection of {@code String} based on the length
     * and then case-insensitive natural ordering, the comparator can be
     * composed using following code,
     *
     * <pre>{@code
     *     Comparator<String> cmp = Comparator.comparingInt(String::length)
     *             .thenComparing(String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param  other the other comparator to be used when this comparator
     *         compares two objects that are equal.
     * @return a lexicographic-order comparator composed of this and then the
     *         other comparator
     */
    fun <T> Comparator<T>.thenCompare(other: Comparator<in T>): Comparator<T> {
        return Comparator<T> { c1, c2 ->
            val res: Int = compare(c1, c2)
            if (res != 0) res else other.compare(c1, c2)
        }
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a key to be compared with the given {@code Comparator}.
     *
     * @implSpec This default implementation behaves as if {@code
     *           thenComparing(comparing(keyExtractor, cmp))}.
     *
     * @param  <U>  the type of the sort key
     * @param  keyExtractor the function used to extract the sort key
     * @param  keyComparator the {@code Comparator} used to compare the sort key
     * @return a lexicographic-order comparator composed of this comparator
     *         and then comparing on the key extracted by the keyExtractor function
     * @see #comparing(Function, Comparator)
     * @see #thenCompare(Comparator)
     */
    fun <T, U> Comparator<T>.thenCompare(
        keyExtractor: (T) -> U,
        keyComparator: Comparator<in U>
    ): Comparator<T> {
        return thenCompare(compare(keyExtractor, keyComparator))
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code Comparable} sort key.
     *
     * @implSpec This default implementation behaves as if {@code
     *           thenComparing(comparing(keyExtractor))}.
     *
     * @param  <U>  the type of the {@link Comparable} sort key
     * @param  keyExtractor the function used to extract the {@link
     *         Comparable} sort key
     * @return a lexicographic-order comparator composed of this and then the
     *         {@link Comparable} sort key.
     * @see #comparing(Function)
     * @see #thenComparing(Comparator)
     */
    fun <T, U : Comparable<U>> Comparator<T>.thenCompare(
        keyExtractor: (T) -> U
    ): Comparator<T> {
        return thenCompare(compare(keyExtractor))
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code int} sort key.
     *
     * @implSpec This default implementation behaves as if {@code
     *           thenComparing(comparingInt(keyExtractor))}.
     *
     * @param  keyExtractor the function used to extract the integer sort key
     * @return a lexicographic-order comparator composed of this and then the
     *         {@code int} sort key
     * @see #comparingInt(ToIntFunction)
     * @see #thenComparing(Comparator)
     */
    fun <T> Comparator<T>.thenCompareInt(keyExtractor: (T) -> Int): Comparator<T> {
        return thenCompare(compare(keyExtractor))
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code long} sort key.
     *
     * @implSpec This default implementation behaves as if {@code
     *           thenComparing(comparingLong(keyExtractor))}.
     *
     * @param  keyExtractor the function used to extract the long sort key
     * @return a lexicographic-order comparator composed of this and then the
     *         {@code long} sort key
     * @see #comparingLong(ToLongFunction)
     * @see #thenComparing(Comparator)
     */
    fun <T> Comparator<T>.thenCompareLong(keyExtractor: (T) -> Long): Comparator<T> {
        return thenCompare(compareLong(keyExtractor))
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code double} sort key.
     *
     * @implSpec This default implementation behaves as if {@code
     *           thenComparing(comparingDouble(keyExtractor))}.
     *
     * @param  keyExtractor the function used to extract the double sort key
     * @return a lexicographic-order comparator composed of this and then the
     *         {@code double} sort key
     * @see #comparingDouble(ToDoubleFunction)
     * @see #thenComparing(Comparator)
     */
    fun <T> Comparator<T>.thenCompareDouble(keyExtractor: (T) -> Double): Comparator<T> {
        return thenCompare(compareDouble(keyExtractor))
    }

    /**
     * Returns a comparator that imposes the reverse of the <em>natural
     * ordering</em>.
     *
     * <p>The returned comparator is serializable.
     *
     * @param  <T> the {@link Comparable} type of element to be compared
     * @return a comparator that imposes the reverse of the <i>natural
     *         ordering</i> on {@code Comparable} objects.
     * @see Comparable
     */
    fun <T> reverseOrder(): Comparator<T> {
        return Collections.reverseOrder()
    }

    /**
     * Accepts a function that extracts a sort key from a type {@code T}, and
     * returns a {@code Comparator<T>} that compares by that sort key using
     * the specified {@link Comparator}.
     *
     * <p>The returned comparator is serializable if the specified function
     * and comparator are both serializable.
     *
     * @apiNote
     * For example, to obtain a {@code Comparator} that compares {@code
     * Person} objects by their last name ignoring case differences,
     *
     * <pre>{@code
     *     Comparator<Person> cmp = Comparator.comparing(
     *             Person::getLastName,
     *             String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param  <T> the type of element to be compared
     * @param  <U> the type of the sort key
     * @param  keyExtractor the function used to extract the sort key
     * @param  keyComparator the {@code Comparator} used to compare the sort key
     * @return a comparator that compares by an extracted key using the
     *         specified {@code Comparator}
     */
    fun <T, U> compare(
        keyExtractor: (T) -> U,
        keyComparator: Comparator<U>
    ): Comparator<T> {
        return Comparator { c1: T, c2: T ->
            keyComparator.compare(
                keyExtractor(c1),
                keyExtractor(c2)
            )
        }
    }

    /**
     * Accepts a function that extracts a {@link java.lang.Comparable
     * Comparable} sort key from a type {@code T}, and returns a {@code
     * Comparator<T>} that compares by that sort key.
     *
     * <p>The returned comparator is serializable if the specified function
     * is also serializable.
     *
     * @apiNote
     * For example, to obtain a {@code Comparator} that compares {@code
     * Person} objects by their last name,
     *
     * <pre>{@code
     *     Comparator<Person> byLastName = Comparator.comparing(Person::getLastName);
     * }</pre>
     *
     * @param  <T> the type of element to be compared
     * @param  <U> the type of the {@code Comparable} sort key
     * @param  keyExtractor the function used to extract the {@link
     *         Comparable} sort key
     * @return a comparator that compares by an extracted key
     */
    fun <T, U : Comparable<U>> compare(
        keyExtractor: (T) -> U
    ): Comparator<T> {
        return Comparator { c1, c2 ->
            keyExtractor(c1).compareTo(keyExtractor(c2))
        }
    }

    /**
     * Accepts a function that extracts an {@code int} sort key from a type
     * {@code T}, and returns a {@code Comparator<T>} that compares by that
     * sort key.
     *
     * <p>The returned comparator is serializable if the specified function
     * is also serializable.
     *
     * @param  <T> the type of element to be compared
     * @param  keyExtractor the function used to extract the integer sort key
     * @return a comparator that compares by an extracted key
     * @see #compare(Function)
     */
    fun <T> compareInt(keyExtractor: (T) -> Int): Comparator<T> {
        return Comparator { c1, c2 ->
            keyExtractor(c1).compareTo(keyExtractor(c2))
        }
    }

    /**
     * Accepts a function that extracts a {@code long} sort key from a type
     * {@code T}, and returns a {@code Comparator<T>} that compares by that
     * sort key.
     *
     * <p>The returned comparator is serializable if the specified function is
     * also serializable.
     *
     * @param  <T> the type of element to be compared
     * @param  keyExtractor the function used to extract the long sort key
     * @return a comparator that compares by an extracted key
     * @see #comparing(Function)
     */
    fun <T> compareLong(keyExtractor: (T) -> Long): Comparator<T> {
        return Comparator { c1, c2 ->
            keyExtractor(c1).compareTo(keyExtractor(c2))
        }
    }

    /**
     * Accepts a function that extracts a {@code double} sort key from a type
     * {@code T}, and returns a {@code Comparator<T>} that compares by that
     * sort key.
     *
     * <p>The returned comparator is serializable if the specified function
     * is also serializable.
     *
     * @param  <T> the type of element to be compared
     * @param  keyExtractor the function used to extract the double sort key
     * @return a comparator that compares by an extracted key
     * @see #compare(Function)
     */
    fun <T> compareDouble(keyExtractor: (T) -> Double): Comparator<T> {
        Objects.requireNonNull(keyExtractor)
        return Comparator { c1, c2 ->
            keyExtractor(c1).compareTo(keyExtractor(c2))
        }
    }
}