/**
	 * File: Utility.java
	 * Author: Brian Borowski
	 * Modified by Victor Dibia
	 * Date created: March 2000
	 * Date last modified: Feb 10, 2012
	 * 
	 * Generate random array of numbers such that the puzzle is always solvable
	 */
package com.denvycom.gidigames;

import java.util.Random;

public class Utility {
	
	public static byte[] getRandomArray(final int numOfTiles, final boolean keepZeroInCorner) {
        final byte[] tiles = new byte[numOfTiles];
        for (int i = numOfTiles - 2; i >= 0; --i) {
            tiles[i] = (byte)(i + 1);
        }
        tiles[numOfTiles - 1] = 0;

        int maxTilesToSwap;
        if (keepZeroInCorner) {
            maxTilesToSwap = numOfTiles - 1;
        } else {
            maxTilesToSwap = numOfTiles;
        }
        final Random random = new Random();
        for (int i = 49; i >= 0; --i) {
            final int rand1 = random.nextInt(maxTilesToSwap);
            int rand2 = random.nextInt(maxTilesToSwap);
            if (rand1 == rand2) {
                if (rand1 < (maxTilesToSwap << 1)) {
                    rand2 = random.nextInt(maxTilesToSwap - rand1) + rand1;
                } else {
                    rand2 = random.nextInt(rand1);
                }
            }
            swap(rand1, rand2, tiles);
        }
        if (!isValidPermutation(tiles)) {
            if (tiles[0] != 0 && tiles[1] != 0) {
                swap(0, 1, tiles);
            } else {
                swap(2, 3, tiles);
            }
        }
        return tiles;
    }

    public static boolean isValidPermutation(final byte[] state) {
        final int numOfTiles = state.length,
                  dim = (int)Math.sqrt(numOfTiles);
        int inversions = 0;

        for (int i = 0; i < numOfTiles; ++i) {
            final byte iTile = state[i];
            if (iTile != 0) {
                for (int j = i + 1; j < numOfTiles; ++j) {
                    final byte jTile = state[j];
                    if (jTile != 0 && jTile < iTile) {
                        ++inversions;
                    }
                }
            } else {
                if ((dim & 0x1) == 0) {
                    inversions += (1 + i / dim);
                }
            }
        }
        if ((inversions & 0x1) == 1) return false;
        return true;
    }

    public static String byteArrayToString(final byte[] state) {
        final int numOfTiles = state.length;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numOfTiles; ++i) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(state[i]);
        }
        return builder.toString();
    }
    public static int[] byteArrayToIntArray(final byte[] state) {
    	final int numOfTiles = state.length;
        final int[] intarray = new int[numOfTiles];
        for (int i = 0; i < numOfTiles; ++i) {
            intarray[i] = state[i];
        }
        return intarray ;
    }
    private static void swap(final int i, final int j, final byte[] A) {
        final byte temp = A[i];
        A[i] = A[j];
        A[j] = temp;
    }
}
