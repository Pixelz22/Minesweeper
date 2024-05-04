package Game;

import Engine.RenderableObject;

import java.awt.*;

public class BoxIcon extends RenderableObject {
    public Color mainColor;
    public Color outlineColor;
    public int outlineWidth;

    public BoxIcon(Color color) {
        this(color, color, 0);
    }
    public BoxIcon(Color mainColor, Color outlineColor, int outlineWidth) {
        this.mainColor = mainColor;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
    }

    @Override
    public void render() {
        if (outlineWidth > 0) {
            targetCanvas.getTextureGraphics().setColor(outlineColor);
            targetCanvas.getTextureGraphics().fillRect(rect.getGlobalCornerX(), rect.getGlobalCornerY(),
                    rect.getSizeX(), rect.getSizeY());
        }

        targetCanvas.getTextureGraphics().setColor(mainColor);
        targetCanvas.getTextureGraphics().fillRect(rect.getGlobalCornerX() + outlineWidth, rect.getGlobalCornerY() + outlineWidth,
                rect.getSizeX() - (2 * outlineWidth), rect.getSizeY() - (2 * outlineWidth));
    }
}
