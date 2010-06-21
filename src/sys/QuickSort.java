/*
    SBTVD TS Parser - MPEG-2 Transport Stream analyser and debugging tool.
    Copyright (C) 2010 Gabriel A. G. Marques
    gabriel.marques@gmail.com
	
    This file is part of the "SBTVD Transport Stream Parser" program.

    The SBTVD Transport Stream Parser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The SBTVD Transport Stream Parser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the SBTVD Stream Parser.  If not, see <http://www.gnu.org/licenses/>.
 
 */
package sys;

public class QuickSort {
	/***************************************************************************
	 * Adapted from Quicksort code from Sedgewick 7.1, 7.2.
	 **************************************************************************/
	public static void sort(Comparable[] set) {
		quicksort(set, 0, set.length - 1);
	}

	public static void sort(Comparable[] set, int numbOfItems) {
		quicksort(set, 0, numbOfItems - 1);
	}

	// quicksort a[left] to a[right]
	public static void quicksort(Comparable[] key, int left, int right) {
		if (right <= left)
			return;
		int i = partition(key, left, right);
		quicksort(key, left, i - 1);
		quicksort(key, i + 1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(Comparable[] key, int left, int right) {
		int i = left - 1;
		int j = right;
		while (true) {
			while (key[++i].getValue() < key[right].getValue())
				// find item on left to swap
				; // a[right] acts as sentinel
			while (key[right].getValue() < key[--j].getValue())
				// find item on right to swap
				if (j == left)
					break; // don't go out-of-bounds
			if (i >= j)
				break; // check if pointers cross
			exch(key, i, j); // swap two elements into place
		}
		exch(key, i, right); // swap with partition element
		return i;
	}

	public interface Comparable {
		public int getValue();
	}

	// exchange a[i] and a[j]
	private static void exch(Object[] a, int i, int j) {
		Object swap = a[i];
		a[i] = a[j];
		a[j] = swap;
	}
}
