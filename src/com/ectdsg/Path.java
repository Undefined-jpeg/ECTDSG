package com.ectdsg;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Path {
    public List<Point> points = new ArrayList<>();

    public Path(int width, int height) {
        addPoint(0, (int)(height * 0.10));
        addPoint((int)(width * 0.20), (int)(height * 0.10));
        addPoint((int)(width * 0.20), (int)(height * 0.30));
        addPoint((int)(width * 0.40), (int)(height * 0.30));
        addPoint((int)(width * 0.40), (int)(height * 0.50));
        addPoint((int)(width * 0.60), (int)(height * 0.50));
        addPoint((int)(width * 0.60), (int)(height * 0.70));
        addPoint((int)(width * 0.80), (int)(height * 0.70));
        addPoint((int)(width * 0.80), (int)(height * 0.90));
        addPoint(width, (int)(height * 0.90));
    }

    public void addPoint(int x, int y) {
        points.add(new Point(x, y));
    }

    public Point getPoint(int index) {
        if (index < 0 || index >= points.size()) {
            return points.get(points.size() - 1);
        }
        return points.get(index);
    }

    public int getLength() {
        return points.size();
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(163, 163, 163));
        g2d.setStroke(new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        g2d.setStroke(new BasicStroke(1));
    }
}
