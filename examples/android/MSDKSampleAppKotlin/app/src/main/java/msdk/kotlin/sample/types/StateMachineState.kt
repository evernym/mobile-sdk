package msdk.kotlin.sample.types

enum class StateMachineState(val value: Int) {
    NONE(0), INITIALIZED(1), OFFER_SENT(2), REQUEST_RECEIVED(3), ACCEPTED(4), UNFULFILLED(5), EXPIRED(
        6
    ),
    REVOKED(7), REDIRECTED(8), REJECTED(9);

    fun matches(state: Int): Boolean {
        return value == state
    }

}