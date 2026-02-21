package com.fuchsbau.shorin.Races.Base;

public final class Age implements LifeStages {

    private int age;
    private final int adultAge;
    private final int grownAge;
    private final int lifeExpectancy;
    private final int maxAge;

    public Age(int age, int adultAge, int grownAge, int lifeExpectancy) {
        this.age = Math.max(0, age);
        this.adultAge = adultAge;
        this.grownAge = grownAge;
        this.lifeExpectancy = lifeExpectancy;

        // Cap: +50% der lifeExpectancy
        int adultSpan = Math.max(1, lifeExpectancy - adultAge);
        this.maxAge = lifeExpectancy + (adultSpan / 2);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    public void addYears(int years) {
        this.age = Math.max(0, this.age + years);
    }

    public int adultAge() {
        return adultAge;
    }

    public int grownAge() {
        return grownAge;
    }

    public int lifeExpectancy() {
        return lifeExpectancy;
    }

    @Override
    public int ageAdult() {
        return adultAge;
    }

    // Middle/Old sind gameplay-Definitionen; hier: Prozent vom Lebensbogen
    @Override
    public int ageMiddle() {
        // ~55% zwischen adult und lifeExpectancy
        return adultAge + Math.round((lifeExpectancy - adultAge) * 0.55f);
    }

    @Override
    public int ageOld() {
        // ~85% zwischen adult und lifeExpectancy (kurz vor Anfälligkeit)
        return adultAge + Math.round((lifeExpectancy - adultAge) * 0.85f);
    }

    @Override
    public int ageMax() {
        return maxAge;
    }

    @Override
    public Boolean canDieNaturally() {
        return age >= lifeExpectancy;
    }

    /***
     * % Chance zu sterben, zwischen 0 und 1
     * Maximal Monatlich aufrufen !!
     * Kein Täglich !!
     */
    @Override
    public float dyingChance() {
        if (age < lifeExpectancy) return 0f;
        if (age >= maxAge) return 1f;

        // linear 0..1
        float t = (age - lifeExpectancy) / (float) (maxAge - lifeExpectancy);
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t;
    }
}