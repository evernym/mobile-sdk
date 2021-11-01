package msdk.kotlin.sample.utils

import java9.util.concurrent.CompletableFuture as CompletableFuture9
import java.util.concurrent.CompletableFuture as CompletableFuture

fun <T> CompletableFuture9<T>.wrap(): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    this.whenComplete { res, ex ->
        if (ex != null) {
            future.completeExceptionally(ex)
        } else {
            future.complete(res)
        }
    }
    return future
}