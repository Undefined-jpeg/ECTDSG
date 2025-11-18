package com.ectdsg;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Path2 extends Path {

    public Path2(int width, int height) {
        super(width, height);
        points = new ArrayList<>();
        addPoint(width / 2, 0);
        addPoint(width / 2, height / 2);
        addPoint(0, height / 2);
        addPoint(width, height / 2);
    }
}
