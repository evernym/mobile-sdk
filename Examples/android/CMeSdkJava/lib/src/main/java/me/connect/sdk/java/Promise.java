/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package me.connect.sdk.java;

import java.util.Map;

/*
 * Interface that represents a JavaScript Promise which can be passed to the native module as a
 * method parameter.
 *
 * Methods annotated with {@link ReactMethod} that use a {@link Promise} as the last parameter
 * will be marked as "promise" and will return a promise when invoked from JavaScript.
 */
public interface Promise<T> {

    /**
     * Successfully resolve the Promise with an optional value.
     *
     * @param value T
     */
    void resolve(T value);

    /**
     * Report an error without an exception using a custom code and error message.
     *
     * @param code    String
     * @param message String
     */
    void reject(String code, String message);

    /**
     * Report an exception with a custom code.
     *
     * @param code      String
     * @param throwable Throwable
     */
    void reject(String code, Throwable throwable);

    /**
     * Report an exception with a custom code and error message.
     *
     * @param code      String
     * @param message   String
     * @param throwable Throwable
     */
    void reject(String code, String message, Throwable throwable);


    /**
     * Report an exception, with default error code.
     * Useful in catch-all scenarios where it's unclear why the error occurred.
     *
     * @param throwable Throwable
     */
    void reject(Throwable throwable);

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
    void reject(Throwable throwable, Map userInfo);

    /**
     * Reject with a code and userInfo Map.
     *
     * @param code     String
     * @param userInfo Map
     */
    void reject(String code, Map userInfo);

    /**
     * Report an exception with a custom code and userInfo.
     *
     * @param code      String
     * @param throwable Throwable
     * @param userInfo  Map
     */
    void reject(String code, Throwable throwable, Map userInfo);

    /**
     * Report an error with a custom code, error message and userInfo,
     * an error not caused by an exception.
     *
     * @param code     String
     * @param message  String
     * @param userInfo Map
     */
    void reject(String code, String message, Map userInfo);

    /**
     * Report an exception with a custom code, error message and userInfo.
     *
     * @param code      String
     * @param message   String
     * @param throwable Throwable
     * @param userInfo  Map
     */
    void reject(String code, String message, Throwable throwable, Map userInfo);

    /* ------------
     *  Deprecated
     * ------------ */

    /**
     * Report an error which wasn't caused by an exception.
     *
     * @deprecated Prefer passing a module-specific error code to JS.
     * Using this method will pass the error code "EUNSPECIFIED".
     */
    @Deprecated
    void reject(String message);
}
