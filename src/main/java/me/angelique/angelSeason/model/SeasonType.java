package me.angelique.angelSeason.model;

public enum SeasonType {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;

    public SeasonType next() {
        SeasonType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
