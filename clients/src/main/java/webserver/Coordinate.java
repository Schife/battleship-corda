package webserver;

import java.io.Serializable;
import java.util.List;

public class Coordinate implements Serializable {
    private String x;
    private String y;

    public Coordinate(String x, String y) {
        this.x = x;
        this.y = y;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
