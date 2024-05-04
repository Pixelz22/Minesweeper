package Engine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.channels.ShutdownChannelGroupException;

public class Texture2D {
    private int width, height;
    private BufferedImage buffer1Image, buffer2Image;
    private Graphics2D buffer1Graphics, buffer2Graphics;

    private boolean currentEditBuffer; // False if editing at buffer1, true if editing at buffer2

    public Texture2D(int width, int height, int type) {
        this.width = width;
        this.height = height;

        buffer1Image = new BufferedImage(width, height, type);
        buffer2Image = new BufferedImage(width, height, type);

        buffer1Graphics = buffer1Image.createGraphics();
        buffer2Graphics = buffer2Image.createGraphics();
    }

    public BufferedImage getCurrent() {
        if (currentEditBuffer) return buffer1Image;
        return buffer2Image;
    }

    public Graphics2D getGraphics() {
        if (currentEditBuffer) return buffer2Graphics;
        return buffer1Graphics;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Switches which buffer is being edited and which is displayed.
    public void applyChanges() {
        currentEditBuffer = !currentEditBuffer;
    }
}
