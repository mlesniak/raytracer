package com.mlesniak.raytracer.math;

import java.text.DecimalFormat;

/**
 * Vector3D library.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Vector3D {
    public double x;
    public double y;
    public double z;

    public Vector3D() {
        // Necessary for YAML parser.
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D copy() {
        return new Vector3D(x, y, z);
    }

    public Vector3D path(Vector3D vec) {
        return new Vector3D(vec.x - x, vec.y - y, vec.z - z);
    }

    public Vector3D scale(double factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public double dot(Vector3D vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public double distance(Vector3D vec) {
        double x1 = x - vec.x;
        double y1 = y - vec.y;
        double z1 = z - vec.z;
        return Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        double len = length();
        return new Vector3D(x / len, y / len, z / len);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.###");

        return "Vector3D{" +
                "x=" + df.format(x) +
                ", y=" + df.format(y) +
                ", z=" + df.format(z) +
                '}';
    }

    public Vector3D crossProduct(Vector3D vec) {
        return new Vector3D(
                y * vec.z - z * vec.y,
                z * vec.x - x * vec.z,
                x * vec.y - y * vec.x);
    }

    public Vector3D plus(Vector3D vec) {
        return new Vector3D(
                x + vec.x,
                y + vec.y,
                z + vec.z);
    }

    public Vector3D minus(Vector3D vec) {
        return new Vector3D(
                x - vec.x,
                y - vec.y,
                z - vec.z);
    }
}

