package Engine;

public abstract class RenderableObject {
    public TRect rect = new TRect();
    protected TCanvas targetCanvas;

    public void setTargetCanvas(TCanvas canvas) {
        targetCanvas = canvas;
    }
    public TCanvas getTargetCanvas() {
        return targetCanvas;
    }

    public void flagTargetCanvas() {
        targetCanvas.flagForRendering();
    }

    public abstract void render();

}
