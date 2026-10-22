package io.github.xienaoban.biologydictionary.gui;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;

/**
 * Rotation of the entity models rendered on the entity cards. Every screen owns
 * one instance and refreshes it once per frame, so all the cards of a screen stay
 * in sync.
 */
@ClientOnly
public final class EntityDisplayRotation {
    private float rotateX;
    private float rotateY;

    public void update(long currTime) {
        int t = (int) (currTime % 8000);
        t = t > 4000 ? 6000 - t : t - 2000;
        rotateX = (float) Math.atan(t / 400F);
        rotateY = (float) Math.atan(45 / 40F);
    }

    public float getRotateX() { return rotateX; }

    public float getRotateY() { return rotateY; }
}
