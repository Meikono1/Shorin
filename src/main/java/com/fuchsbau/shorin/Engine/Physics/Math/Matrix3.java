package com.fuchsbau.shorin.Engine.Physics.Math;

import java.util.Stack;

public class Matrix3 {
    //Done
    private static final double[] reverse_eqns = new double[18];
    public double[] elements;

    public Matrix3() {
        elements = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
    }

    public Matrix3(double[] elements) {
        this.elements = elements;
    }

    public void identity() {
        elements[0] = 1;
        elements[1] = 0;
        elements[2] = 0;

        elements[3] = 0;
        elements[4] = 1;
        elements[5] = 0;

        elements[6] = 0;
        elements[7] = 0;
        elements[8] = 1;
    }

    public void setZero() {
        elements[0] = 0;
        elements[1] = 0;
        elements[2] = 0;
        elements[3] = 0;
        elements[4] = 0;
        elements[5] = 0;
        elements[6] = 0;
        elements[7] = 0;
        elements[8] = 0;
    }

    public void setTrace(Vec3 vector) {
        elements[0] = vector.x;
        elements[4] = vector.y;
        elements[8] = vector.z;
    }

    public Vec3 getTrace(Vec3 vector) {
        return new Vec3(elements[0], elements[4], elements[8]);
    }

    public Vec3 vmult(Vec3 vector, Vec3 target) {
        Vec3 mul = vmult(vector);
        target.set(mul.x, mul.y, mul.z);
        return target;
    }

    public Vec3 vmult(Vec3 vector) {
        Vec3 ret = new Vec3();
        ret.x = elements[0] * vector.x + elements[1] * vector.y + elements[2] * vector.z;
        ret.y = elements[3] * vector.x + elements[4] * vector.y + elements[5] * vector.z;
        ret.z = elements[6] * vector.x + elements[7] * vector.y + elements[8] * vector.z;

        return ret;
    }

    public void smult(double s) {
        for (int i = 0; i < this.elements.length; i++) {
            this.elements[i] *= s;
        }
    }

    public Matrix3 mmult(Matrix3 matrix, Matrix3 target) {
        Matrix3 zwi = mmult(matrix);
        target.setElements(zwi.elements);
        return target;
    }

    public Matrix3 mmult(Matrix3 matrix) {
        Matrix3 ret = new Matrix3();

        double a11 = elements[0],
                a12 = elements[1],
                a13 = elements[2],
                a21 = elements[3],
                a22 = elements[4],
                a23 = elements[5],
                a31 = elements[6],
                a32 = elements[7],
                a33 = elements[8];

        double b11 = matrix.elements[0],
                b12 = matrix.elements[1],
                b13 = matrix.elements[2],
                b21 = matrix.elements[3],
                b22 = matrix.elements[4],
                b23 = matrix.elements[5],
                b31 = matrix.elements[6],
                b32 = matrix.elements[7],
                b33 = matrix.elements[8];

        ret.elements[0] = a11 * b11 + a12 * b21 + a13 * b31;
        ret.elements[1] = a11 * b12 + a12 * b22 + a13 * b32;
        ret.elements[2] = a11 * b13 + a12 * b23 + a13 * b33;

        ret.elements[3] = a21 * b11 + a22 * b21 + a23 * b31;
        ret.elements[4] = a21 * b12 + a22 * b22 + a23 * b32;
        ret.elements[5] = a21 * b13 + a22 * b23 + a23 * b33;

        ret.elements[6] = a31 * b11 + a32 * b21 + a33 * b31;
        ret.elements[7] = a31 * b12 + a32 * b22 + a33 * b32;
        ret.elements[8] = a31 * b13 + a32 * b23 + a33 * b33;

        return ret;
    }

    public Matrix3 scale(Vec3 vector, Matrix3 target) {
        Matrix3 zwi = scale(vector);
        target.setElements(zwi.elements);
        return target;
    }

    public Matrix3 scale(Vec3 vector) {
        Matrix3 ret = new Matrix3();
        for (int i = 0; i < 3; i++) {
            ret.elements[3 * i + 0] = vector.x * elements[3 * i + 0];
            ret.elements[3 * i + 1] = vector.y * elements[3 * i + 1];
            ret.elements[3 * i + 2] = vector.z * elements[3 * i + 2];
        }
        return ret;
    }

    public void setElements(double[] elements) {
        this.elements = elements;
    }

    public Vec3 solve(Vec3 vector, Vec3 target) {
        Vec3 zwi = solve(vector);
        target.set(zwi.x, zwi.y, zwi.z);
        return target;
    }

    public Vec3 solve(Vec3 vector) {
        Vec3 target = new Vec3();
        int nr = 3, nc = 4;
        double[] eqns = new double[nr * nc];

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                eqns[i + nc * j] = this.elements[i + 3 * j];

        eqns[3] = vector.x;
        eqns[3 + 4] = vector.y;
        eqns[3 + 8] = vector.z;

        int n = 3, k = n, kp = 4;
        do {
            int i = k - n;
            if (eqns[i + nc * i] == 0) {
                for (int j = i + 1; j < k; j++) {
                    if (eqns[i + nc * j] != 0) {
                        int np = kp;
                        do {
                            int p = kp - np;
                            eqns[p + nc * i] += eqns[p + nc * j];
                        } while (--np != 0);
                        break;
                    }
                }
            }
            if (eqns[i + nc * i] != 0) {
                for (int j = i + 1; j < k; j++) {
                    double multiplier = eqns[i + nc * j] / eqns[i + nc * i];
                    int np = kp;
                    do {
                        int p = kp - np;
                        eqns[p + nc * j] = p <= i ? 0 : eqns[p + nc * j] - eqns[p + nc * i] * multiplier;
                    } while (--np != 0);
                }
            }
        } while (--n != 0);

        target.z = eqns[2 * nc + 3] / eqns[2 * nc + 2];
        target.y = (eqns[1 * nc + 3] - eqns[1 * nc + 2] * target.z) / eqns[1 * nc + 1];
        target.x = (eqns[0 * nc + 3] - eqns[0 * nc + 2] * target.z - eqns[0 * nc + 1] * target.y) / eqns[0 * nc + 0];

        if (Double.isNaN(target.x) || Double.isNaN(target.y) || Double.isNaN(target.z) ||
                Double.isInfinite(target.x) || Double.isInfinite(target.y) || Double.isInfinite(target.z)) {
            throw new RuntimeException("Could not solve equation! Got x=" + target + ", vector=" + vector);
        }

        return target;
    }

    public Matrix3 reverse(Matrix3 target) {
        if (target == null) target = new Matrix3();

        int nr = 3, nc = 6;
        double[] eqns = reverse_eqns;

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                eqns[i + nc * j] = this.elements[i + 3 * j];

        eqns[3 + 6 * 0] = 1; eqns[3 + 6 * 1] = 0; eqns[3 + 6 * 2] = 0;
        eqns[4 + 6 * 0] = 0; eqns[4 + 6 * 1] = 1; eqns[4 + 6 * 2] = 0;
        eqns[5 + 6 * 0] = 0; eqns[5 + 6 * 1] = 0; eqns[5 + 6 * 2] = 1;

        int n = 3, k = n, kp = nc;
        do {
            int i = k - n;
            if (eqns[i + nc * i] == 0) {
                for (int j = i + 1; j < k; j++) {
                    if (eqns[i + nc * j] != 0) {
                        int np = kp;
                        do {
                            int p = kp - np;
                            eqns[p + nc * i] += eqns[p + nc * j];
                        } while (--np != 0);
                        break;
                    }
                }
            }
            if (eqns[i + nc * i] != 0) {
                for (int j = i + 1; j < k; j++) {
                    double multiplier = eqns[i + nc * j] / eqns[i + nc * i];
                    int np = kp;
                    do {
                        int p = kp - np;
                        eqns[p + nc * j] = p <= i ? 0 : eqns[p + nc * j] - eqns[p + nc * i] * multiplier;
                    } while (--np != 0);
                }
            }
        } while (--n != 0);

        // Obere linke Ecke eliminieren
        int i = 2;
        do {
            int j = i - 1;
            do {
                double multiplier = eqns[i + nc * j] / eqns[i + nc * i];
                int np = nc;
                do {
                    int p = nc - np;
                    eqns[p + nc * j] -= eqns[p + nc * i] * multiplier;
                } while (--np != 0);
            } while (j-- != 0);
        } while (--i != 0);

        // Diagonale normalisieren
        i = 2;
        do {
            double multiplier = 1.0 / eqns[i + nc * i];
            int np = nc;
            do {
                int p = nc - np;
                eqns[p + nc * i] *= multiplier;
            } while (--np != 0);
        } while (i-- != 0);

        // Ergebnis in target schreiben
        i = 2;
        do {
            int j = 2;
            do {
                double p = eqns[nr + j + nc * i];
                if (Double.isNaN(p) || Double.isInfinite(p)) {
                    throw new RuntimeException("Could not reverse! A=" + this);
                }
                target.e(i, j, p);
            } while (j-- != 0);
        } while (i-- != 0);

        return target;
    }

    public Matrix3 setRotationFromQuaternion(Quaternion q) {
        double x = q.x, y = q.y, z = q.z, w = q.w;
        double x2 = x + x, y2 = y + y, z2 = z + z;
        double xx = x * x2, xy = x * y2, xz = x * z2;
        double yy = y * y2, yz = y * z2, zz = z * z2;
        double wx = w * x2, wy = w * y2, wz = w * z2;

        elements[3 * 0 + 0] = 1 - (yy + zz);
        elements[3 * 0 + 1] = xy - wz;
        elements[3 * 0 + 2] = xz + wy;

        elements[3 * 1 + 0] = xy + wz;
        elements[3 * 1 + 1] = 1 - (xx + zz);
        elements[3 * 1 + 2] = yz - wx;

        elements[3 * 2 + 0] = xz - wy;
        elements[3 * 2 + 1] = yz + wx;
        elements[3 * 2 + 2] = 1 - (xx + yy);

        return this;
    }

    public Matrix3 copy(Matrix3 matrix) {
        System.arraycopy(matrix.elements, 0, this.elements, 0, 9);
        return this;
    }

    public Matrix3 transpose(Matrix3 target) {
        if (target == null) target = new Matrix3();

        double[] M = this.elements;
        double[] T = target.elements;

        T[0] = M[0];
        T[4] = M[4];
        T[8] = M[8];

        double tmp;
        tmp = M[1]; T[1] = M[3]; T[3] = tmp;
        tmp = M[2]; T[2] = M[6]; T[6] = tmp;
        tmp = M[5]; T[5] = M[7]; T[7] = tmp;

        return target;
    }

    public double e(int row, int column) {
        return elements[column + 3 * row];
    }

    public void e(int row, int column, double value) {
        elements[column + 3 * row] = value;
    }
}