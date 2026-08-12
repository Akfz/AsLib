package v.akfz.aslib.event.api;

// Добавлять если надо возможность отменить
// isCancelled() -> если true - отменить ивент
public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}