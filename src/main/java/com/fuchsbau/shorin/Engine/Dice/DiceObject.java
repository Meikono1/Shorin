package com.fuchsbau.shorin.Engine.Dice;

public class DiceObject {

    private final DiceType type;
    private final DiceDefinition definition;

    // Mittelpunkt im Canvas / Raum
    private double centerX;
    private double centerY;
    private double centerZ;

    // Rotation
    private double angleX;
    private double angleY;
    private double angleZ;

    // Bewegung
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    // Rotationsgeschwindigkeit
    private double angularVelocityX;
    private double angularVelocityY;
    private double angularVelocityZ;

    // Darstellung / Kollision
    private double scale;
    private double radius;

    // Zustand
    private boolean rolling;
    private boolean sleeping;
    private boolean selected;
    private boolean visible = true;

    // Ergebnis
    private int currentFaceValue;
    private int lastStableFaceValue;

    public DiceObject(DiceType type, DiceDefinition definition, double centerX, double centerY, double centerZ) {
        this.type = type;
        this.definition = definition;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.scale = 1.0;
        this.radius = 32.0;
    }

    public DiceType getType() {
        return type;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public double getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(double centerZ) {
        this.centerZ = centerZ;
    }

    public double getAngleX() {
        return angleX;
    }

    public void setAngleX(double angleX) {
        this.angleX = angleX;
    }

    public double getAngleY() {
        return angleY;
    }

    public void setAngleY(double angleY) {
        this.angleY = angleY;
    }

    public double getAngleZ() {
        return angleZ;
    }

    public void setAngleZ(double angleZ) {
        this.angleZ = angleZ;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
    }

    public void setVelocityZ(double velocityZ) {
        this.velocityZ = velocityZ;
    }

    public double getAngularVelocityX() {
        return angularVelocityX;
    }

    public void setAngularVelocityX(double angularVelocityX) {
        this.angularVelocityX = angularVelocityX;
    }

    public double getAngularVelocityY() {
        return angularVelocityY;
    }

    public void setAngularVelocityY(double angularVelocityY) {
        this.angularVelocityY = angularVelocityY;
    }

    public double getAngularVelocityZ() {
        return angularVelocityZ;
    }

    public void setAngularVelocityZ(double angularVelocityZ) {
        this.angularVelocityZ = angularVelocityZ;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isRolling() {
        return rolling;
    }

    public void setRolling(boolean rolling) {
        this.rolling = rolling;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getCurrentFaceValue() {
        return currentFaceValue;
    }

    public void setCurrentFaceValue(int currentFaceValue) {
        this.currentFaceValue = currentFaceValue;
    }

    public int getLastStableFaceValue() {
        return lastStableFaceValue;
    }

    public void setLastStableFaceValue(int lastStableFaceValue) {
        this.lastStableFaceValue = lastStableFaceValue;
    }

    public void stopMotion() {
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        angularVelocityX = 0;
        angularVelocityY = 0;
        angularVelocityZ = 0;
        rolling = false;
        sleeping = true;
    }

    public void wakeUp() {
        sleeping = false;
        rolling = true;
    }

    public DiceDefinition getDefinition() {
        return definition;
    }
}