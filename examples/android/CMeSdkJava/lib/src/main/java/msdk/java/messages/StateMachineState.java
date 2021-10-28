package msdk.java.messages;

public enum StateMachineState {
    NONE(0),
    INITIALIZED(1),
    OFFER_SENT(2),
    REQUEST_RECEIVED(3),
    ACCEPTED(4),
    UNFULFILLED(5),
    EXPIRED(6),
    REVOKED(7),
    REDIRECTED(8),
    REJECTED(9);

    private final int value;

    StateMachineState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean matches(int state) {
        return this.value == state;
    }
}
