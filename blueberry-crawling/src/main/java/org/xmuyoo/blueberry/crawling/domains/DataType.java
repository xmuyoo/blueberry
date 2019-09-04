package org.xmuyoo.blueberry.crawling.domains;

public enum DataType {
    StockPrice("StockPrice");

    final String name;

    DataType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
