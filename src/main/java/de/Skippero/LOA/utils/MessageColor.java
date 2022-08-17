package de.Skippero.LOA.utils;

import java.awt.*;

public enum MessageColor {

    CYAN(Color.CYAN), MAGENTA(Color.MAGENTA), ORANGE(Color.ORANGE), BLUE(Color.BLUE), RED(Color.red), GREEN(Color.GREEN);

    private Color color;

    MessageColor(Color color) {
        this.color = color;
    }

    public static MessageColor getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }

    public Color getColor() {
        return this.color;
    }

}