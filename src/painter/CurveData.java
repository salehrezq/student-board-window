/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author S
 */
public class CurveData implements Serializable {

     private static final long serialVersionUID = 83204520674491L;

    private Color color;
    private float stroke;
    private boolean eraser;
    private ArrayList<Point> pointsList;

    public Color getColor() {
        return color;
    }

    public float getStroke() {
        return this.stroke;
    }

    public ArrayList<Point> getPointsList() {
        return pointsList;
    }

    public boolean isEraser() {
        return eraser;
    }

}
