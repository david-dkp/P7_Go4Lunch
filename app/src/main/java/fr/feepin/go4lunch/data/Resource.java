package fr.feepin.go4lunch.data;

import javax.annotation.Nullable;

public class Resource<T> {

    private T data;
    private String message;

    public Resource(@Nullable T data, @Nullable String message) {
        this.data = data;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Success<T> extends Resource<T> {

        public Success(T data, String message) {
            super(data, message);
        }
    }

    public static class Loading<T> extends Resource<T> {

        public Loading(T data, String message) {
            super(data, message);
        }
    }

    public static class Error<T> extends Resource<T> {


        public Error(T data, String message) {
            super(data, message);
        }
    }
}
