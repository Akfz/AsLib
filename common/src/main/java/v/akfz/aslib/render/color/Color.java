package v.akfz.aslib.render.color;

import java.util.Objects;

public class Color {
    private double red;
    private double green;
    private double blue;
    private double alpha;

    public Color(Object red, Object green, Object blue, Object alpha) {
        setFields(red, green, blue, alpha);
    }

    public Color(Object red, Object green, Object blue) {
        setFields(red, green, blue, 255);
    }

    public Color(Object main) {
        setFields(main, main, main, 255);
    }

    public Color(Object main, Object alpha) {
        setFields(main, main, main, alpha);
    }

    public Color(java.awt.Color c) {
        setFields(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
    }

    private void setFields(Object r, Object g, Object b, Object a) {
        this.red = normalize(r);
        this.green = normalize(g);
        this.blue = normalize(b);
        this.alpha = normalize(a);
    }

    public void setRed(Object val) {
        this.red = normalize(val);
    }

    public void setGreen(Object val) {
        this.green = normalize(val);
    }

    public void setBlue(Object val) {
        this.blue = normalize(val);
    }

    public void setAlpha(Object val) {
        this.alpha = normalize(val);
    }

    public double getRed() {
        return this.red;
    }

    public double getGreen() {
        return this.green;
    }

    public double getBlue() {
        return this.blue;
    }

    public double getAlpha() {
        return this.alpha;
    }

    private double normalize(Object obj) {
        if (obj instanceof Number num) {
            double val = num.doubleValue();
            if (val > 1.0) {
                return val / 255.0;
            }
            return Math.max(0.0, val);
        }
        return 0.0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof Color col)) return false;

        return Double.compare(col.red, red) == 0 &&
                Double.compare(col.green, green) == 0 &&
                Double.compare(col.blue, blue) == 0 &&
                Double.compare(col.alpha, alpha) == 0;
    }

    @Override
    public String toString() {
        return "Color : " +
                "Red : " + red +
                " , Green : " + green +
                " , Blue: " + blue +
                " , Alpha: " + alpha;
    }
}
