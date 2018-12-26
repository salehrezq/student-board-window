/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package painter;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

/**
 *
 * @author S
 */
public class PointData implements Serializable {

    private static final long serialVersionUID = 34705201206374L;


    private float diameter;
    private Color color;
    private Point point;

    public PointData(Point point) {
        this.point = point;
    }

    public float getDiameter() {
        return diameter;
    }

    public void setDiameter(float stroke) {
        this.diameter = stroke;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

}
