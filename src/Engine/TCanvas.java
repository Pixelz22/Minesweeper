package Engine;

import Utils.TUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TCanvas extends JPanel
    implements MouseListener {

    Texture2D mainTexture;
    BufferedImage scaled;
    public TRect rect;

    Object interpolationSetting;

    Color blankColor = Color.BLACK;
    Color clearColor;

    ArrayList<RenderableObject> toRender = new ArrayList<>();
    boolean renderFlag = true;
    ArrayList<TButton> buttons = new ArrayList<>();

    public TCanvas(int width, int height, Object interpolationSetting) {
        this(width, height, interpolationSetting, Color.WHITE);
    }

    public TCanvas(int width, int height, Object interpolationSetting, Color clearColor) {
        mainTexture = new Texture2D(width, height, BufferedImage.TYPE_INT_RGB);
        rect = new TRect();
        rect.setSize(width, height);
        scaled = mainTexture.getCurrent();
        this.interpolationSetting = interpolationSetting;
        this.clearColor = clearColor;

        addMouseListener(this);
    }

    public RenderableObject addRenderable(RenderableObject obj) {
        //toRender.add(obj);
        TUtils.indexedBasedBinaryInsert(toRender, obj, (obj1, obj2) -> Float.compare(obj1.rect.getGlobalZOffset(), obj2.rect.getGlobalZOffset()));
        obj.setTargetCanvas(this);
        return obj;
    }
    public TButton addButton(TButton button) {
        //buttons.add(button);
        TUtils.indexedBasedBinaryInsert(buttons, button, (obj1, obj2) -> Float.compare(obj1.rect.getGlobalZOffset(), obj2.rect.getGlobalZOffset()));
        return button;
    }

    // Utility function to tell the canvas to redraw itself. Saves time on frames where nothing happens.
    public void flagForRendering() {
        renderFlag = true;
    }

    public void render() {
        if (renderFlag) {
            getTextureGraphics().setColor(clearColor);
            getTextureGraphics().fillRect(0, 0, mainTexture.getWidth(), mainTexture.getHeight());
            for (RenderableObject obj : toRender) {
                if (obj.rect.isGlobalActive()) obj.render();
            }
            mainTexture.applyChanges();
            generateScaledInstance();
            renderFlag = false;
        }
    }

    //region Getters and Setters

    public Graphics2D getTextureGraphics() {
        return mainTexture.getGraphics();
    }

    public Texture2D getTexture() {
        return mainTexture;
    }

    public Object getInterpolationSetting() {
        return interpolationSetting;
    }
    public void setInterpolationSetting(Object interpSetting) {
        interpolationSetting = interpSetting;
    }

    public Color getBlankColor() {
        return blankColor;
    }
    public void setBlankColor(Color color) {
        blankColor = color;
    }

    //endregion

    //region JPanel Functions

    @Override
    public Dimension getPreferredSize () {
        //return mainTexture == null ? new Dimension(200, 200) : new Dimension(mainTexture.getWidth() * 2, mainTexture.getHeight() * 2);
        return new Dimension(1760, 990);
    }

    @Override
    public void invalidate () {
        super.invalidate();
        generateScaledInstance();
    }

    @Override
    protected void paintComponent (Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (scaled != null) {
            if (blankColor != null) {
                g2d.setColor(blankColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            int x = (getWidth() - scaled.getWidth()) / 2;
            int y = (getHeight() - scaled.getHeight()) / 2;
            g2d.drawImage(scaled, x, y, this);
        }
        g2d.dispose();
    }

    //endregion

    //region Resize Functions

    protected void generateScaledInstance () {
        if (mainTexture != null) {
            scaled = getScaledInstanceToFill(getSize());
        }
    }

    public BufferedImage getScaledInstanceToFill (Dimension size){
        float scaleFactor = getScaleFactorToFill(size);
        return getScaledInstance(scaleFactor);
    }

    public float getScaleFactorToFill (Dimension size){
        float scale = 1f;
        if (mainTexture != null) {
            int imageWidth = mainTexture.getWidth();
            int imageHeight = mainTexture.getHeight();
            float scaleX = (float) size.getWidth() / (float) imageWidth;
            float scaleY = (float) size.getHeight() / (float) imageHeight;
            scale = Math.min(scaleX, scaleY);
        }
        return scale;
    }

    public BufferedImage getScaledInstance ( double scaleFactor){
        BufferedImage imgBuffer = null;
        imgBuffer = getScaledInstance(scaleFactor, interpolationSetting, true);
        return imgBuffer;
    }

    public BufferedImage getScaledInstance ( double scaleFactor, Object hint,boolean higherQuality){
        BufferedImage scaled = mainTexture.getCurrent();
        if (scaleFactor != 1.0) {
            if (scaleFactor > 1.0) {
                scaled = getScaledUpInstance(scaleFactor, hint, higherQuality);
            } else if (scaleFactor > 0.0) {
                scaled = getScaledDownInstance(scaleFactor, hint, higherQuality);
            }
        }

        return scaled;
    }

    protected BufferedImage getScaledDownInstance ( double scaleFactor, Object hint,boolean higherQuality){

        int targetWidth = (int) Math.round(mainTexture.getCurrent().getWidth() * scaleFactor);
        int targetHeight = (int) Math.round(mainTexture.getCurrent().getHeight() * scaleFactor);

        int type = (mainTexture.getCurrent().getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = mainTexture.getCurrent();

        if (targetHeight > 0 || targetWidth > 0) {
            int w, h;
            if (higherQuality) {
                w = mainTexture.getCurrent().getWidth();
                h = mainTexture.getCurrent().getHeight();
            } else {
                w = targetWidth;
                h = targetHeight;
            }

            do {
                if (higherQuality && w > targetWidth) {
                    w /= 2;
                    if (w < targetWidth) {
                        w = targetWidth;
                    }
                }

                if (higherQuality && h > targetHeight) {
                    h /= 2;
                    if (h < targetHeight) {
                        h = targetHeight;
                    }
                }

                BufferedImage tmp = new BufferedImage(Math.max(w, 1), Math.max(h, 1), type);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g2.drawImage(ret, 0, 0, w, h, null);
                g2.dispose();

                ret = tmp;
            } while (w != targetWidth || h != targetHeight);
        } else {
            ret = new BufferedImage(1, 1, type);
        }
        return ret;
    }

    protected BufferedImage getScaledUpInstance ( double scaleFactor, Object hint,boolean higherQuality){

        int targetWidth = (int) Math.round(mainTexture.getCurrent().getWidth() * scaleFactor);
        int targetHeight = (int) Math.round(mainTexture.getCurrent().getHeight() * scaleFactor);

        int type = (mainTexture.getCurrent().getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = mainTexture.getCurrent();

        if (targetHeight > 0 || targetWidth > 0) {
            int w, h;
            if (higherQuality) {
                w = mainTexture.getCurrent().getWidth();
                h = mainTexture.getCurrent().getHeight();
            } else {
                w = targetWidth;
                h = targetHeight;
            }

            do {
                if (higherQuality && w < targetWidth) {
                    w *= 2;
                    if (w > targetWidth) {
                        w = targetWidth;
                    }
                }

                if (higherQuality && h < targetHeight) {
                    h *= 2;
                    if (h > targetHeight) {
                        h = targetHeight;
                    }
                }

                BufferedImage tmp = new BufferedImage(Math.max(w, 1), Math.max(h, 1), type);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g2.drawImage(ret, 0, 0, w, h, null);
                g2.dispose();

                ret = tmp;
            } while (w != targetWidth || h != targetHeight);
        } else {
            ret = new BufferedImage(1, 1, type);
        }
        return ret;
    }

    //endregion

    //region Mouse Listener
    public int windowSpaceToCanvasSpaceX(int xInWindow) {
        float u = (xInWindow - (getWidth() - scaled.getWidth()) / 2f) / (float) scaled.getWidth();
        return (int) (u * mainTexture.getWidth());
    }

    public int windowSpaceToCanvasSpaceY(int yInWindow) {
        float v = (yInWindow - (getHeight() - scaled.getHeight()) / 2f) / (float) scaled.getHeight();
        return (int) (v * mainTexture.getHeight());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = windowSpaceToCanvasSpaceX(e.getX());
        int mouseY = windowSpaceToCanvasSpaceY(e.getY());
        System.out.println("Mouse was clicked at " + mouseX + ", " + mouseY);

        for (int i = buttons.size() - 1; i >= 0; i--) {
            TButton button = buttons.get(i);
            if (!button.rect.isGlobalActive()) continue;
            int[] bounds = button.rect.getGlobalBounds();
            if (mouseX >= bounds[0] && mouseY >= bounds[1]
                    && mouseX < bounds[2] && mouseY < bounds[3]) {
                button.onClicked(e);
                break;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    //endregion
}
