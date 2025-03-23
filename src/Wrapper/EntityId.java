package Wrapper;

public class EntityId {
    private String entityId;

    public EntityId(String subscriberId) {
        this.entityId = subscriberId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String subscriberId) {
        this.entityId = subscriberId;
    }
}
