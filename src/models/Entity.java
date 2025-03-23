package models;

import Wrapper.EntityId;

public class Entity {
    private EntityId entityId;
    private String ipAddress;
    private String name;

    public Entity(EntityId entityId, String ipAddress, String name) {
        this.entityId = entityId;
        this.ipAddress = ipAddress;
        this.name = name;
    }

    public EntityId getEntityId() {
        return entityId;
    }

    public void setEntityId(EntityId entityId) {
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
