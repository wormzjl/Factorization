package factorization.truth;

public abstract class AbstractPage {
    abstract void draw(DocViewer doc, int ox, int oy, String hoveredLink);
    void closed() {}
    void mouseDragStart() {}
    void mouseDrag(int dx, int dy) {}
}
