package hayo.util;

public record IntRange(int start, int end) {
    public boolean contains(int index) {
        return index >= this.start && index < this.end;
    }
}
