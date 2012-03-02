package com.bourke.finch;

public interface TwitterTaskCallback<T, U> {
    void onSuccess(T result);
    void onFailure(U exception);
}
