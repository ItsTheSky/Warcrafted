package net.itsthesky.warcrafted.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Range between two integers, e.g. '1-5' will be random between 1 and 5.
 */
@Getter
@AllArgsConstructor
public class IntRange {

	private final int min;
	private final int max;

	public IntRange(String range) {
		if (range.contains("-")) {
			final String[] split = range.split("( )?-( )?");
			this.min = Integer.parseInt(split[0]);
			this.max = Integer.parseInt(split[1]);
		} else {
			this.min = Integer.parseInt(range);
			this.max = Integer.parseInt(range);
		}
	}

	public int get() {
		return (int) (Math.random() * (max - min + 1) + min);
	}
}
