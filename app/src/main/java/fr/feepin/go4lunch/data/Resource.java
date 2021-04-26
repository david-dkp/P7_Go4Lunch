package fr.feepin.go4lunch.data;

public class Resource<T>{

    public static class Success<T> extends Resource<T> {

        private T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    public static class Loading<T> extends Resource<T> {

        private T data;
        private String message;

        public Loading(T data, String message) {
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
    }

    public static class Error<T> extends Resource<T> {

        private String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
