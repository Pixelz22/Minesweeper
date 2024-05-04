package Utils;

import java.util.Objects;

public class Vector2Int {
    public int x, y;
    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        Vector2Int other = (Vector2Int) obj;
        return other.x == x && other.y == y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
