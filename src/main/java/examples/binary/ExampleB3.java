package examples.binary;

/**
 * Demonstrates de/serialization in binary
 * for bitmasked rows, assuming:
 * <p>
 * For each field in a row, the bitmask
 * encodes 0 for null and 1 for non-null.
 * <p>
 * For a non-row, the bitmask encodes either
 * all-0's for null or all-1's for a tombstone.
 */
public class ExampleB3 {
	public static void main(String[] args) {
		// Encode each bit in sequence.
		short mask = 0;
		mask = (short) ((mask << 1) + 1); // 5th bit
		mask = (short) ((mask << 1) + 0); // 4th bit
		mask = (short) ((mask << 1) + 0); // 3rd bit
		mask = (short) ((mask << 1) + 1); // 2nd bit
		mask = (short) ((mask << 1) + 0); // 1st bit
		mask = (short) ((mask << 1) + 0); // 0th bit
		explain(mask);

		// Mutate arbitrary bits.
		mask = (short) (mask | (1 << 3)); // set bit on
		mask = (short) (mask & ~(1 << 2)); // set bit off
		mask = (short) (mask ^ (1 << 4)); // toggle bit
		explain(mask);

		// Access arbitrary bits.
		for (int i = 5; i >= 0; i--) {
			boolean on = (mask & (1 << i)) != 0;
			System.out.printf("Bit %2s   %s\n".formatted(i, on ? "on" : "off"));
		}

		// Encode null instead of row.
		short null_mask = 0;
		explain(null_mask);

		// Encode tombstone instead of row.
		short tombstone_mask = -1;
		explain(tombstone_mask);
	}

	public static void explain(short mask) {
		System.out.printf(
			"Dec %2s = Bin %8s (%s)\n",
			mask,
			bits(mask),
			mask == 0 ? "null" : mask < 0 ? "tombstone" : "row"
		);
	}

	public static String bits(short mask) {
		return "%16s"
			.formatted(Integer.toBinaryString(mask & 0xFFFF))
			.replace(' ', '0');
	}
}