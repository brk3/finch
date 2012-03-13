package com.bourke.finch.common;

public interface TwitterTaskCallback<T, U> {
    void onSuccess(T result);
    void onFailure(U exception);
}
