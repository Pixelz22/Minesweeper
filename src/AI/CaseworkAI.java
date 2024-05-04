package AI;

import Game.Minefield;
import Utils.TUtils;
import Utils.Vector2Int;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class CaseworkAI {

    public int[][] generalGrid;

    public enum TileID {
        HIDDEN(9),
        FLAGGED(10),
        REVEALED(11);

        public final int id;

        TileID(int id) {
            this.id = id;
        }
    }

    private int gridWidth;
    private int gridHeight;

    public boolean isMarker(int gridX, int gridY) {
        return generalGrid[gridY][gridX] < TileID.HIDDEN.id;
    }
    public boolean isHidden(int gridX, int gridY) {
        return generalGrid[gridY][gridX] == TileID.HIDDEN.id;
    }
    public boolean isFlag(int gridX, int gridY) {
        return generalGrid[gridY][gridX] == TileID.FLAGGED.id;
    }
    public boolean isRevealedMine(int gridX, int gridY) {
        return generalGrid[gridY][gridX] == TileID.REVEALED.id;
    }

    public void importGrid(Minefield minefield) {
        generalGrid = new int[minefield.getHeight()][minefield.getWidth()];
        gridHeight = minefield.getHeight();
        gridWidth = minefield.getWidth();

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (minefield.getCellFogAt(x, y) == Minefield.CellFogType.HIDDEN) generalGrid[y][x] = TileID.HIDDEN.id;
                else if (minefield.getCellFogAt(x, y) == Minefield.CellFogType.FLAGGED) generalGrid[y][x] = TileID.FLAGGED.id;
                else if (minefield.checkForMineAt(x, y)) generalGrid[y][x] = TileID.REVEALED.id;
                else generalGrid[y][x] = minefield.getMarkerAt(x, y);
            }
        }
    }

    // Active spaces are ones with hidden tiles around them
    public Vector2Int[] determineActiveSpaces() {
        LinkedList<Vector2Int> spacesToAdd = new LinkedList<>();
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {

                if (isMarker(x, y)) {
                    boolean hasHiddenNeighbor = false;
                    boolean hasMarkedNeighbor = false;



                    for (int i = -2; i <= 2; i++) {
                        if (hasMarkedNeighbor && hasHiddenNeighbor) break; // break out of loop early to save time
                        for (int j = -2; j <= 2; j++) {
                            if (i == 0 && j == 0) continue;
                            int neighborX = x + i;
                            int neighborY = y + j;
                            if (neighborX < 0 || neighborX >= gridWidth || neighborY < 0 || neighborY >= gridHeight) continue;

                            if (Math.abs(i) <= 1 && Math.abs(j) <= 1 && isHidden(neighborX, neighborY)) hasHiddenNeighbor = true;
                            if (isMarker(neighborX, neighborY)) hasMarkedNeighbor = true;
                        }
                    }

                    if (hasMarkedNeighbor && hasHiddenNeighbor) spacesToAdd.add(new Vector2Int(x, y));

                }

            }
        }

        Vector2Int[] spaceArray = new Vector2Int[spacesToAdd.size()];
        return spacesToAdd.toArray(spaceArray);
    }


    public HashSet<HashMap<Vector2Int, Integer>> determinePossibilities() {
        // Focus Maps allow us to only look at a certain portion of the grid.
        // This means we can only worry about copying data around the active spaces where all the changes will be happening.
        // Saves a decent bit of memory and time when the board is large.

        Vector2Int[] activeSpaces = determineActiveSpaces();
        HashSet<HashMap<Vector2Int, Integer>> possibleFocusMaps = new HashSet<>();
        if (activeSpaces.length == 0) return possibleFocusMaps;

        LinkedList<FocusMapConstruct> constructsToMake = new LinkedList<>();
        constructsToMake.add(new FocusMapConstruct());

        while (!constructsToMake.isEmpty()) {
            FocusMapConstruct currentConstruct = constructsToMake.pop();

            Vector2Int currentActiveSpace = activeSpaces[currentConstruct.nextActiveSpaceID];


            // STEP 1: Determine the base settings so we can create the possibilities.
            // This data lets us create a general structure for all possible layouts that
            // could stem from the current construct.

            int hiddenTiles = 0;
            int remainingMines = generalGrid[currentActiveSpace.y][currentActiveSpace.x];

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    int neighborX = currentActiveSpace.x + j;
                    int neighborY = currentActiveSpace.y + i;
                    if (neighborX < 0 || neighborX >= gridWidth || neighborY < 0 || neighborY >= gridHeight) continue;
                    Vector2Int neighbor = new Vector2Int(neighborX, neighborY);

                    if (isFlag(neighborX, neighborY)) remainingMines--; // Don't include already-known flags in the focus map, but be sure to count them towards remaining mines
                    else if (!isMarker(neighborX, neighborY)) { // Don't include markers in the focus map
                        currentConstruct.focusMap.putIfAbsent(neighbor, generalGrid[neighborY][neighborX]); // Add the new tiles around the currentActiveSpace

                        int tileID = currentConstruct.focusMap.get(neighbor);
                        if (tileID == TileID.HIDDEN.id) hiddenTiles++;
                        if (tileID == TileID.FLAGGED.id || tileID == TileID.REVEALED.id) remainingMines--;
                    }
                }
            }

            if (remainingMines < 0) continue; // if there are too many flags, invalidate this construct


            // STEP 2: Create the possible mine layouts

            String temp = ""; // construct the basic layout template that contains how many mines and blanks are remaining
            for (int i = 0; i < hiddenTiles; i++) {
                if (i < remainingMines) temp += "1";
                else temp += "0";
            }
            HashSet<String> possibleLayoutTemplates = TUtils.permutations(temp);

            for (String layoutTemplate : possibleLayoutTemplates) {
                int positionInTemplate = 0;
                HashMap<Vector2Int, Integer> newFocusMap = currentConstruct.cloneFocusMap();
                HashSet<Vector2Int> newIllegals = currentConstruct.cloneIllegals();

                boolean isValidLayout = true;

                for (int i = -1; i <= 1; i++) {
                    if (!isValidLayout) break;

                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        int neighborX = currentActiveSpace.x + j;
                        int neighborY = currentActiveSpace.y + i;
                        if (neighborX < 0 || neighborX >= gridWidth || neighborY < 0 || neighborY >= gridHeight) continue;
                        Vector2Int neighbor = new Vector2Int(neighborX, neighborY);

                        if (newFocusMap.containsKey(neighbor) && newFocusMap.get(neighbor) == TileID.HIDDEN.id) { // Layout Template only considers hidden spaces, so only count thos
                            if (layoutTemplate.charAt(positionInTemplate) == '0') newIllegals.add(neighbor);
                            else if (layoutTemplate.charAt(positionInTemplate) == '1') {
                                if (newIllegals.contains(neighbor)) { // tried to put a flag in an illegal space. Invalidate the layout
                                    isValidLayout = false;
                                    break;
                                }
                                else {
                                    newFocusMap.put(neighbor, TileID.FLAGGED.id);
                                }
                            }
                            positionInTemplate++; // move to the next character in the template
                        }

                    }
                }

                if (!isValidLayout) continue; //if the layout isn't valid, don't bother to add it back to the queue. Skip to the next

                // STEP 3: If it's valid, add it to list of possible maps if it's finished, else put it back into the queue to be further developed
                if (currentConstruct.nextActiveSpaceID + 1 >= activeSpaces.length) {
                    possibleFocusMaps.add(newFocusMap);
                }
                else {
                    FocusMapConstruct newConstruct = new FocusMapConstruct();
                    newConstruct.focusMap = newFocusMap;
                    newConstruct.illegalSpaces = newIllegals;
                    newConstruct.nextActiveSpaceID = currentConstruct.nextActiveSpaceID + 1; // move to next active space
                    constructsToMake.add(newConstruct);
                }
            }
        }

        return possibleFocusMaps;
    }

    public HashMap<Vector2Int, Float> determineProbabilities() {
        HashSet<HashMap<Vector2Int, Integer>> possibilities = determinePossibilities();

        HashMap<Vector2Int, Integer> flagOccurrenceMap = new HashMap<>();
        int possibilitiesLogged = 0;

        for (HashMap<Vector2Int, Integer> p : possibilities) { // for each possibility
            for (Vector2Int tile : p.keySet()) { // tally up the flags
                flagOccurrenceMap.putIfAbsent(tile, 0);
                if (p.get(tile) == TileID.FLAGGED.id) flagOccurrenceMap.put(tile, flagOccurrenceMap.get(tile) + 1);
            }
            possibilitiesLogged++;
        }

        HashMap<Vector2Int, Float> ret = new HashMap<>();
        for (Vector2Int tile : flagOccurrenceMap.keySet()) { // compute the percent chance of a flag in every given tile
            ret.put(tile, (float) flagOccurrenceMap.get(tile) / possibilitiesLogged);
        }
        return ret;
    }

    // Simple Data Structure to use for data storage in the queue for determinePossibilities()
    private class FocusMapConstruct {
        public int nextActiveSpaceID = 0;
        public HashMap<Vector2Int, Integer> focusMap = new HashMap<>();

        public HashSet<Vector2Int> illegalSpaces = new HashSet<>();

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != FocusMapConstruct.class) return false;
            FocusMapConstruct other = (FocusMapConstruct) obj;
            return (nextActiveSpaceID == other.nextActiveSpaceID) &&
                    (focusMap == other.focusMap) && (illegalSpaces == other.illegalSpaces);
        }

        public HashMap<Vector2Int, Integer> cloneFocusMap() {
            HashMap<Vector2Int, Integer> clone = new HashMap<>();
            for (Vector2Int key : focusMap.keySet()) {
                clone.put(key, focusMap.get(key));
            }
            return clone;
        }

        public HashSet<Vector2Int> cloneIllegals() {
            return new HashSet<>(illegalSpaces);
        }
        
    }
}
