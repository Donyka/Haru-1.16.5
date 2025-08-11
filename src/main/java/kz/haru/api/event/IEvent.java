package kz.haru.api.event;

public interface IEvent {
    void setCancelled(boolean cancelled);

    boolean isCancelled();
}
