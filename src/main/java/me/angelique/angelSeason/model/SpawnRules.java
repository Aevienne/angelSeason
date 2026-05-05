package me.angelique.angelSeason.model;

import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SpawnRules {

    private final Set<EntityType> allowedNatural;
    private final Set<EntityType> blockedNatural;
    private final Set<String> allowedCustomMobIds;
    private final Set<EntityType> bloodMoonExtraAllowed;
    private final Set<String> bloodMoonExtraCustomIds;

    public SpawnRules(Set<EntityType> allowedNatural, Set<EntityType> blockedNatural, Set<String> allowedCustomMobIds, Set<EntityType> bloodMoonExtraAllowed, Set<String> bloodMoonExtraCustomIds) {
        this.allowedNatural = allowedNatural == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(allowedNatural));
        this.blockedNatural = blockedNatural == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(blockedNatural));
        this.allowedCustomMobIds = allowedCustomMobIds == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(allowedCustomMobIds));
        this.bloodMoonExtraAllowed = bloodMoonExtraAllowed == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(bloodMoonExtraAllowed));
        this.bloodMoonExtraCustomIds = bloodMoonExtraCustomIds == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(bloodMoonExtraCustomIds));
    }

    public Set<EntityType> getAllowedNatural() {
        return allowedNatural;
    }

    public Set<EntityType> getBlockedNatural() {
        return blockedNatural;
    }

    public Set<String> getAllowedCustomMobIds() {
        return allowedCustomMobIds;
    }

    public Set<EntityType> getBloodMoonExtraAllowed() {
        return bloodMoonExtraAllowed;
    }

    public Set<String> getBloodMoonExtraCustomIds() {
        return bloodMoonExtraCustomIds;
    }
}
