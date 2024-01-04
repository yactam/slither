package com.poo.slither.model;

import javafx.scene.paint.Color;

import java.io.Serializable;

public interface Segment extends Serializable {
    double getX();
    double getY();
    void moveTo(double x, double y);
    Color getColor();
    boolean handelCollision(Serpent serpent1, Serpent serpent2);
    Nourriture toFood();
}
