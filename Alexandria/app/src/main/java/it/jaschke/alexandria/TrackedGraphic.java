package it.jaschke.alexandria;

import it.jaschke.alexandria.CameraPreview.GraphicOverlay;

/**
 * Created by chyupa on 16-Sep-15.
 */
abstract class TrackedGraphic<T> extends GraphicOverlay.Graphic {
    private int mId;

    TrackedGraphic(GraphicOverlay overlay) {
        super(overlay);
    }

    void setId(int id) {
        mId = id;
    }

    protected int getId() {
        return mId;
    }

    abstract void updateItem(T item);
}
