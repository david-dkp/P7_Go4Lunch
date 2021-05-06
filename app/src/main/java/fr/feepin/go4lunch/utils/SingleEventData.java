package fr.feepin.go4lunch.utils;

public class SingleEventData<T> {

    private final T data;
    private boolean hasBeenSeen = false;

    public SingleEventData(T data) {
        this.data = data;
    }

    public T getData() {
        if (!hasBeenSeen) {
            hasBeenSeen = true;
            return this.data;
        } else {
            return null;
        }
    }

    public T peekData() {
        return this.data;
    }
}
