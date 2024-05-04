package Game;

import AI.CaseworkAI;
import Engine.*;
import Utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;

public class Game {

    private static JFrame mainFrame;
    private static TCanvas canvas;

    Minefield minefield;

    public static TCanvas getCanvas() {
        return canvas;
    }

    private static LinkedList<Runnable> preRenderEvents = new LinkedList<>();

    public static void queuePreRenderEvent(Runnable event) {
        preRenderEvents.add(event);
    }

    private static long startTime, deltaTime;

    public static long getStartTime() {
        return startTime;
    }

    public static long getDeltaTime() {
        return deltaTime;
    }

    public Game() {
        //region Graphics Initialization
        canvas = new TCanvas(256, 256,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, Color.white);

        mainFrame = new JFrame("Minesweeper");
        mainFrame.add(canvas);
        mainFrame.setResizable(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.requestFocusInWindow();
        mainFrame.setVisible(true);
        //endregion

        initGameObjects();

        //region Time Initialization
        long tick = 0;
        startTime = System.nanoTime();
        long previousFrameTime;
        long currentFrameTime = startTime;
        //endregion

        while (true) {
            //region Time events
            tick++;
            System.out.println("next tick: " + tick);
            previousFrameTime = currentFrameTime;
            currentFrameTime = System.nanoTime();

            deltaTime = currentFrameTime - previousFrameTime;
            System.out.println("dT (ns): " + deltaTime);
            System.out.println("Average Tickrate (tps): " + (tick * 1000000000.0) / (currentFrameTime - startTime));
            //endregion

            System.out.println("Performing PreRender Events..");
            while (!preRenderEvents.isEmpty()) {
                preRenderEvents.remove().run();
            }

            System.out.println("Rendering...");
            canvas.render();
            mainFrame.repaint();

            System.out.println();
        }
    }

    private void initGameObjects() {
        //region Minefield
        minefield = (Minefield) canvas.addRenderable(new Minefield(16, 30, 99));
        minefield.spreadMines();
        minefield.rect.parent = canvas.rect;
        minefield.rect.setSize(128, 250);
        minefield.rect.setLocalAnchorPos(0.5f, 0.5f);
        minefield.rect.setParentAnchorPos(0.5f, 0.5f);
        minefield.rect.setOffset(0, 0);
        //endregion

        //region Minefield Button
        TButton minefieldButton = canvas.addButton(new TButton());
        minefieldButton.addOnClickEvent((e) -> minefield.onClicked(e));
        minefieldButton.rect.parent = minefield.rect;
        minefieldButton.rect.setSize(128, 250);
        minefieldButton.rect.setLocalAnchorPos(0.5f, 0.5f);
        minefieldButton.rect.setParentAnchorPos(0.5f, 0.5f);
        minefieldButton.rect.setOffset(0, 0);
        //endregion

        //region AI Test Button
        BoxIcon aiBox = (BoxIcon) canvas.addRenderable(new BoxIcon(Color.GRAY, Color.BLACK, 2));
        aiBox.rect.parent = canvas.rect;
        aiBox.rect.setSize(50, 50);
        aiBox.rect.setLocalAnchorPos(0f, 0f);
        aiBox.rect.setParentAnchorPos(0f, 0f);
        aiBox.rect.setOffset(0, 0);
        aiBox.rect.setZOffset(1);

        CaseworkAI ai = new CaseworkAI();
        TButton aiBoxButton = canvas.addButton(new TButton());
        aiBoxButton.addOnClickEvent((e) -> {
            ai.importGrid(minefield);
            HashMap<Vector2Int, Float> probabilities = ai.determineProbabilities(); // Run the AI
            for (Vector2Int tile : probabilities.keySet()) {
                if (probabilities.get(tile) == 1) minefield.interactAtCellPos(tile, MouseEvent.BUTTON3); // Simulate Right Click if it's a guaranteed flag tile
                else if (probabilities.get(tile) == 0) minefield.interactAtCellPos(tile, MouseEvent.BUTTON1); // Simulate left click if it's a guaranteed empty tile
            }
        });
        aiBoxButton.rect.parent = aiBox.rect;
        aiBoxButton.rect.setSize(150, 50);
        aiBoxButton.rect.setLocalAnchorPos(0.5f, 0.5f);
        aiBoxButton.rect.setParentAnchorPos(0.5f, 0.5f);
        aiBoxButton.rect.setOffset(0, 0);
        //endregion
    }
}
