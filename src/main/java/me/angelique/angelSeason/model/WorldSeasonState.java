package me.angelique.angelSeason.model;

public final class WorldSeasonState {

    private SeasonType currentSeason = SeasonType.SPRING;
    private long seasonStartedAtMillis = System.currentTimeMillis();
    private long worldDayCounter = 0L;
    private boolean bloodMoonActive = false;
    private long lastCheckedMinecraftDay = -1L;

    public SeasonType getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(SeasonType currentSeason) {
        this.currentSeason = currentSeason;
    }

    public long getSeasonStartedAtMillis() {
        return seasonStartedAtMillis;
    }

    public void setSeasonStartedAtMillis(long seasonStartedAtMillis) {
        this.seasonStartedAtMillis = seasonStartedAtMillis;
    }

    public long getWorldDayCounter() {
        return worldDayCounter;
    }

    public void setWorldDayCounter(long worldDayCounter) {
        this.worldDayCounter = Math.max(0L, worldDayCounter);
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public void setBloodMoonActive(boolean bloodMoonActive) {
        this.bloodMoonActive = bloodMoonActive;
    }

    public long getLastCheckedMinecraftDay() {
        return lastCheckedMinecraftDay;
    }

    public void setLastCheckedMinecraftDay(long lastCheckedMinecraftDay) {
        this.lastCheckedMinecraftDay = lastCheckedMinecraftDay;
    }
}
