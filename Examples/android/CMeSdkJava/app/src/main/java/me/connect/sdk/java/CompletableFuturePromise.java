/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package me.connect.sdk.java;

import java.util.Map;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Consumer;
import java9.util.function.Function;

/*
 * Implementation of {@link Promise} that represents a JavaScript Promise which can be passed to the
 * native module as a method parameter.
 *
 * Methods annotated with {@link ReactMethod} that use a {@link Promise} as the last parameter
 * will be marked as "promise" and will return a promise when invoked from JavaScript.
 */
public class CompletableFuturePromise<T> implements Promise<T> {

    CompletableFuture<T> future;

    public CompletableFuturePromise(Consumer<? super T> resolve, Function<Throwable, ? extends T> reject) {
        future = new CompletableFuture<>();
        future.exceptionally(reject).thenAccept(resolve);
    }

    /**
     * Successfully resolve the Promise with an optional value.
     *
     * @param value Object
     */
    @Override
    public void resolve(T value) {
        future.complete(value);
    }

    /**
     * Report an error without an exception using a custom code and error message.
     *
     * @param code    String
     * @param message String
     */
    @Override
    public void reject(String code, String message) {
        reject(code, message, /*Throwable*/null, /*Map*/null);
    }

    /**
     * Report an exception with a custom code.
     *
     * @param code      String
     * @param throwable Throwable
     */
    @Override
    public void reject(String code, Throwable throwable) {
        reject(code, /*Message*/null, throwable, /*Map*/null);
    }

    /**
     * Report an exception with a custom code and error message.
     *
     * @param code      String
     * @param message   String
     * @param throwable Throwable
     */
    @Override
    public void reject(String code, String message, Throwable throwable) {
        reject(code, message, throwable, /*Map*/null);
    }

    /**
     * Report an exception, with default error code.
     * Useful in catch-all scenarios where it's unclear why the error occurred.
     *
     * @param throwable Throwable
     */
    @Override
    public void reject(Throwable throwable) {
        reject(/*Code*/null, /*Message*/null, throwable, /*Map*/null);
    }

    /* ---------------------------
     *  With userInfo Map
     * --------------------------- */

    /**
     * Report an exception, with default error code, with userInfo.
     * Useful in catch-all scenarios where it's unclear why the error occurred.
     *
     * @param throwable Throwable
     * @param userInfo  Map
     */
    @Override
    public void reject(Throwable throwable, Map userInfo) {
        reject(/*Code*/null, /*Message*/null, throwable, userInfo);
    }

    /**
     * Reject with a code and userInfo Map.
     *
     * @param code     String
     * @param userInfo Map
     */
    @Override
    public void reject(String code, Map userInfo) {
        reject(code, /*Message*/null, /*Throwable*/null, userInfo);
    }

    /**
     * Report an exception with a custom code and userInfo.
     *
     * @param code      String
     * @param throwable Throwable
     * @param userInfo  Map
     */
    @Override
    public void reject(String code, Throwable throwable, Map userInfo) {
        reject(code, /*Message*/null, throwable, userInfo);
    }

    /**
     * Report an error with a custom code, error message and userInfo,
     * an error not caused by an exception.
     *
     * @param code     String
     * @param message  String
     * @param userInfo Map
     */
    @Override
    public void reject(String code, String message, Map userInfo) {
        reject(code, message, /*Throwable*/null, userInfo);
    }

    /**
     * Report an exception with a custom code, error message and userInfo.
     *
     * @param code      String
     * @param message   String
     * @param throwable Throwable
     * @param userInfo  Map
     */
    @Override
    public void reject(
            String code,
            String message,
            Throwable throwable,
            Map userInfo
    ) {
        future.completeExceptionally(new PromiseException(code, message, throwable, userInfo));
    }

    /* ------------
     *  Deprecated
     * ------------ */

    @Override
    @Deprecated
    public void reject(String message) {
        reject(/*Code*/null, message, /*Throwable*/null, /*Map*/null);
    }
}