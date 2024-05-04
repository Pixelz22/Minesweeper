package Game;

import Engine.RenderableObject;
import Utils.Vector2Int;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;

public class Minefield extends RenderableObject {
    private int width, height, totalMines;
    private boolean[][] mineGrid; // Keeps track of location of mines
    private int[][] markerGrid; // Keeps track of how many mines are around each cell
    private CellFogType[][] fogGrid;

    private Random rand = new Random();

    private int cellSize, minefieldCornerX, minefieldCornerY;

    public Minefield(int width, int height, int totalMines) {
        this.width = width;
        this.height = height;
        this.totalMines = totalMines;
        mineGrid = new boolean[height][width];
        fogGrid = new CellFogType[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(fogGrid[y], CellFogType.HIDDEN);
        }
        markerGrid = new int[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public boolean checkForMineAt(int x, int y) {
        return mineGrid[y][x];
    }

    public int getMarkerAt(int x, int y) {
        return markerGrid[y][x];
    }

    public CellFogType getCellFogAt(int x, int y) {
        return fogGrid[y][x];
    }

    public void setCellFogAt(int x, int y, CellFogType fog) {
        fogGrid[y][x] = fog;
        flagTargetCanvas();
    }

    public void spreadMines() {
        for (int i = 0; i < totalMines; i++) {
            int randomY, randomX;
            do {
                randomY = rand.nextInt(height);
                randomX = rand.nextInt(width);
            } while (mineGrid[randomY][randomX]); // Find a cell without a mine
            mineGrid[randomY][randomX] = true;

            for (int deltaX = -1; deltaX <= 1; deltaX++) {
                for (int deltaY = -1; deltaY <= 1; deltaY++) {
                    if (deltaX == 0 && deltaY == 0) continue;
                    if (randomX + deltaX < 0 || randomX + deltaX >= width) continue;
                    if (randomY + deltaY < 0 || randomY + deltaY >= height) continue;
                    markerGrid[randomY + deltaY][randomX + deltaX]++;
                }
            }
        }
    }

    public Vector2Int getCellAtMousePos(int x, int y) {
        int cellX = (x - minefieldCornerX) / cellSize;
        if (cellX < 0 || cellX >= width) cellX = -1;

        int cellY = (y - minefieldCornerY) / cellSize;
        if (cellY < 0 || cellY >= height) cellY = -1;

        return new Vector2Int(cellX, cellY);
    }

    public void onClicked(MouseEvent e) {
        Vector2Int cellPos = getCellAtMousePos(targetCanvas.windowSpaceToCanvasSpaceX(e.getX()), targetCanvas.windowSpaceToCanvasSpaceY(e.getY()));
        if (cellPos.x == -1 || cellPos.y == -1) return;

        interactAtCellPos(cellPos, e.getButton());
    }

    public void interactAtCellPos(Vector2Int cellPos, int buttonType) { // helper function so that AI can interact with board
        CellFogType fogType = getCellFogAt(cellPos.x, cellPos.y);
        if (buttonType == MouseEvent.BUTTON1 && fogType == CellFogType.HIDDEN)
            visionSpreadProtocol(cellPos);
        if (buttonType == MouseEvent.BUTTON3) {
            switch (getCellFogAt(cellPos.x, cellPos.y)) {
                case FLAGGED -> setCellFogAt(cellPos.x, cellPos.y, CellFogType.HIDDEN);
                case HIDDEN -> setCellFogAt(cellPos.x, cellPos.y, CellFogType.FLAGGED);
                case VISIBLE -> {
                    LinkedList<Vector2Int> hiddenCells = new LinkedList<>();
                    int flaggedCells = 0;
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            if (i == 0 && j == 0) continue;
                            int gridX = cellPos.x + i;
                            int gridY = cellPos.y + j;
                            if (gridX < 0 || gridX >= width || gridY < 0 || gridY >= height) continue;

                            if (getCellFogAt(gridX, gridY) == CellFogType.HIDDEN) hiddenCells.add(new Vector2Int(gridX, gridY));
                            if (getCellFogAt(gridX, gridY) == CellFogType.FLAGGED) flaggedCells++;
                            if (getCellFogAt(gridX, gridY) == CellFogType.VISIBLE && checkForMineAt(gridX, gridY)) flaggedCells++;
                        }
                    }

                    if (flaggedCells >= getMarkerAt(cellPos.x, cellPos.y)) visionSpreadProtocol(hiddenCells);
                }
            }
        }
    }

    public void visionSpreadProtocol(Vector2Int startCell) {
        LinkedList<Vector2Int> toClear = new LinkedList<>();
        toClear.add(startCell);
        visionSpreadProtocol(toClear);
    }

    public void visionSpreadProtocol(LinkedList<Vector2Int> toClear) {
        while (!toClear.isEmpty()) {
            Vector2Int current = toClear.pop();
            setCellFogAt(current.x, current.y, CellFogType.VISIBLE);

            if (getMarkerAt(current.x, current.y) == 0 && !checkForMineAt(current.x, current.y)) { // Auto clear cells next to 0 markers
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        int gridX = current.x + i;
                        int gridY = current.y + j;
                        if (gridX < 0 || gridX >= width || gridY < 0 || gridY >= height) continue;

                        Vector2Int nextCell = new Vector2Int(gridX, gridY);
                        if (getCellFogAt(gridX, gridY) == CellFogType.HIDDEN && !toClear.contains(nextCell)) toClear.add(nextCell);
                    }
                }
            }
        }
    }

    private Color[] markerColors = new Color[]{
            new Color(0, 0, 200),
            new Color(0, 150, 0),
            new Color(220, 0, 0),
            new Color(150, 50, 255),
            new Color(50, 0, 0),
            new Color(0, 200, 200),
            Color.BLACK,
            new Color(150, 150, 150)
    };

    @Override
    public void render() {
        targetCanvas.getTextureGraphics().setColor(Color.DARK_GRAY); // Fill background
        targetCanvas.getTextureGraphics().fill3DRect(rect.getGlobalCornerX(), rect.getGlobalCornerY(), rect.getSizeX(), rect.getSizeY(), false);

        cellSize = Math.min(rect.getSizeX() / width, rect.getSizeY() / height); // Find the best cell size
        minefieldCornerX = (int) (rect.getGlobalCornerX() + (rect.getSizeX() - cellSize * width) / 2f); // Upper left corner of the minefield
        minefieldCornerY = (int) (rect.getGlobalCornerY() + (rect.getSizeY() - cellSize * height) / 2f);

        Font mineFont = new Font("Fixed Width", Font.BOLD, cellSize);
        FontMetrics fm = targetCanvas.getTextureGraphics().getFontMetrics(mineFont);
        targetCanvas.getTextureGraphics().setFont(mineFont);


        // Draw cells
        for (int gridX = 0; gridX < width; gridX++) {
            for (int gridY = 0; gridY < height; gridY++) {
                switch (getCellFogAt(gridX, gridY)) {
                    case VISIBLE -> {
                        targetCanvas.getTextureGraphics().setColor(Color.GRAY);
                        targetCanvas.getTextureGraphics().fill3DRect(minefieldCornerX + gridX * cellSize, minefieldCornerY + gridY * cellSize, cellSize, cellSize, false);
                        if (checkForMineAt(gridX, gridY)) { // Mine Case
                            targetCanvas.getTextureGraphics().setColor(Color.RED);
                            targetCanvas.getTextureGraphics().fillOval(minefieldCornerX + gridX * cellSize, minefieldCornerY + gridY * cellSize,
                                    cellSize - 1, cellSize - 1);
                        } else { // Draw Number Marker
                            int marker = getMarkerAt(gridX, gridY);
                            if (marker < 1) break;
                            targetCanvas.getTextureGraphics().setColor(markerColors[marker - 1]);
                            String markerString = Integer.toString(marker);
                            int markerOffsetX = fm.stringWidth(markerString) / 2; // Gotta offset the text so that it appears in the middle of the cell
                            int markerOffsetY = fm.getHeight() / 4; // Not sure why you have to divide by 4, 2 makes more sense, but hey it works so...
                            targetCanvas.getTextureGraphics().drawString(markerString,
                                    minefieldCornerX + (gridX + 0.5f) * cellSize - markerOffsetX,
                                    minefieldCornerY + (gridY + 0.5f) * cellSize + markerOffsetY); // Add the y offset since +y is down the screen
                        }
                    }
                    case FLAGGED -> {
                        targetCanvas.getTextureGraphics().setColor(Color.GREEN);
                        targetCanvas.getTextureGraphics().fill3DRect(minefieldCornerX + gridX * cellSize, minefieldCornerY + gridY * cellSize, cellSize, cellSize, true);
                    }
                    default -> {
                        targetCanvas.getTextureGraphics().setColor(Color.LIGHT_GRAY);
                        targetCanvas.getTextureGraphics().fill3DRect(minefieldCornerX + gridX * cellSize, minefieldCornerY + gridY * cellSize, cellSize, cellSize, true);
                    }
                }
            }
        }
    }

    public enum CellFogType {
        HIDDEN,
        VISIBLE,
        FLAGGED
    }
}

