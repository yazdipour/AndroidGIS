# Private GeoSuite

* [Screenshot](/Screenshot/)

# Calculate Area

```java
    public void project(Point out) {
        out.x = MercatorProjection.longitudeToX(this.longitudeE6 / CONVERSION_FACTOR);
        out.y = MercatorProjection.latitudeToY(this.latitudeE6 / CONVERSION_FACTOR);
    }

        private static final double CONVERSION_FACTOR = 1000000d;

    public static double longitudeToX(double longitude) {
        return (longitude + 180.0) / 360.0;
    }

    public static double latitudeToY(double latitude) {
        double sinLatitude = Math.sin(latitude * (Math.PI / 180));
        return FastMath.clamp(0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI), 0.0, 1.0);
    }

    private double calculateArea() {
        if (points.size() <= 2) return 0;
        Coordinate[] coordinates = new Coordinate[points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p = points.get(i);
            org.oscim.core.Point point = new org.oscim.core.Point();
            p.project(point);
            coordinates[i] = new Coordinate(point.getX(), point.getY());
            if (i == 0) coordinates[points.size()] = coordinates[0];
        }
        Polygon polygon = new GeometryFactory().createPolygon(coordinates);
        polygon.setSRID(3857);
        return polygon.getArea() * 1000000000000000d;
    }
```