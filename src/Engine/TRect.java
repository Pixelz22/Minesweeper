package Engine;

public class TRect {
    // Pretty self-explanatory, don't you think?
    public TRect parent;

    // The offset of the local anchor from the parent anchor, in pixels.
    private int offsetX, offsetY = 0;

    // size of the TRect, in pixels
    private int sizeX, sizeY = 100;

    // extra offset used for rendering order. Default set to 0.1f to naturally put children in front of parents
    private float zOffset = 0.1f;

    // Values from 0 to 1 describing where the anchors are on the object,
    // with 0 being all the way left/up, and 1 being all the way right/down.
    private float localAnchorPosX, localAnchorPosY = 0;
    private float parentAnchorPosX, parentAnchorPosY = 0;

    // Used to determine whether to render the object (also might apply to whether object should be updated later?)
    private boolean isActive = true;

    public void setParentAnchorPos(float percentX, float percentY) {
        parentAnchorPosX = percentX;
        parentAnchorPosY = percentY;
    }
    public float getParentAnchorPosX() {
        return parentAnchorPosX;
    }
    public float getParentAnchorPosY() {
        return parentAnchorPosY;
    }

    public void setOffset(int x, int y) {
        offsetX = x;
        offsetY = y;
    }
    public int getOffsetX() {
        return offsetX;
    }
    public int getOffsetY() {
        return offsetY;
    }

    public void setSize(int x, int y) {
        sizeX = x;
        sizeY = y;
    }
    public int getSizeX() {
        return sizeX;
    }
    public int getSizeY() {
        return sizeY;
    }

    public float getLocalZOffset() {
        return zOffset;
    }
    public float getGlobalZOffset() {
        return zOffset + ((parent == null) ? 0 : parent.getGlobalZOffset());
    }
    public void setZOffset(int zOffset) {
        this.zOffset = zOffset;
    }

    public void setLocalAnchorPos(float percentX, float percentY) {
        localAnchorPosX = percentX;
        localAnchorPosY = percentY;
    }
    public float getLocalAnchorPosX() {
        return localAnchorPosX;
    }
    public float getLocalAnchorPosY() {
        return localAnchorPosY;
    }

    // Gets the X coordinate of the upper-left corner of the TRect, relative to the parent anchor
    public int getLocalCornerX() {
        if (parent == null) return 0;
        return (int) (offsetX - localAnchorPosX * sizeX);
    }

    // Gets the Y coordinate of the upper-left corner of the TRect, relative to the parent anchor
    public int getLocalCornerY() {
        if (parent == null) return 0;
        return (int) (offsetY - localAnchorPosY * sizeY);
    }

    // Gets the X coordinate of the upper-left corner of the TRect, relative to the world position
    public int getGlobalCornerX() {
        if (parent == null) return 0;
        return parent.getGlobalCornerX() + (int) (parent.getSizeX() * parentAnchorPosX) + getLocalCornerX();
    }

    // Gets the Y coordinate of the upper-left corner of the TRect, relative to the world position
    public int getGlobalCornerY() {
        if (parent == null) return 0;
        return parent.getGlobalCornerY() + (int) (parent.getSizeY() * parentAnchorPosY) + getLocalCornerY();
    }

    // Returns a list of [upperLeftCornerX, upperLeftCornerY, bottomRightCornerX, bottomRightCornerY]
    public int[] getGlobalBounds() {
        int cornerX = getGlobalCornerX();
        int cornerY = getGlobalCornerY();
        return new int[]{cornerX, cornerY, cornerX + sizeX, cornerY + sizeY};
    }


    public boolean isLocalActive() {
        return isActive;
    }
    public boolean isGlobalActive() {
        if (parent == null) return isActive;
        return isActive && parent.isGlobalActive();
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    public void toggleActive() {
        isActive = !isActive;
    }
}
