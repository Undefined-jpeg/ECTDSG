package com.ectdsg;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Path3 extends Path {

    public Path3(int width, int height) {
        super(width, height);
        points = new ArrayList<>();
        addPoint(0, 0);
        addPoint(width, height);
        addPoint(0, height);
        addPoint(width, 0);
    }
}
