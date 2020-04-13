package org.xmuyoo.blueberry.collect.domains;

public enum DataType {
    StockPrice("StockPrice"),
    FinancialReport("FinancialReport")
    ;

    final String name;

    DataType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
