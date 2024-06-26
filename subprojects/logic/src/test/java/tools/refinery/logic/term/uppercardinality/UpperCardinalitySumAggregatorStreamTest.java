/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.logic.term.uppercardinality;

import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;

class UpperCardinalitySumAggregatorStreamTest {
	@ParameterizedTest
	@MethodSource
	void testStream(List<UpperCardinality> list, UpperCardinality expected) {
		var result = UpperCardinalitySumAggregator.INSTANCE.aggregateStream(list.stream());
		assertThat(result, Matchers.is(expected));
	}

	static Stream<Arguments> testStream() {
		return Stream.of(
				Arguments.of(List.of(), UpperCardinalities.ZERO),
				Arguments.of(List.of(UpperCardinality.of(3)), UpperCardinality.of(3)),
				Arguments.of(
						List.of(
								UpperCardinality.of(2),
								UpperCardinality.of(3)
						),
						UpperCardinality.of(5)
				),
				Arguments.of(List.of(UpperCardinalities.UNBOUNDED), UpperCardinalities.UNBOUNDED),
				Arguments.of(
						List.of(
								UpperCardinalities.UNBOUNDED,
								UpperCardinalities.UNBOUNDED
						),
						UpperCardinalities.UNBOUNDED
				),
				Arguments.of(
						List.of(
								UpperCardinalities.UNBOUNDED,
								UpperCardinality.of(3)
						),
						UpperCardinalities.UNBOUNDED
				)
		);
	}
}
