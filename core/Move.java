package core;

public record Move(int from, int to, char promotion) {
    public boolean isPromotion() {
        return promotion != 0;
    }
}
