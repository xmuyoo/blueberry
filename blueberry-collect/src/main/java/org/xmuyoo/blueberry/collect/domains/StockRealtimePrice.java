package org.xmuyoo.blueberry.collect.domains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmuyoo.blueberry.collect.Configs;
import org.xmuyoo.blueberry.collect.utils.Utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
public class StockRealtimePrice extends Data {

    public static StockRealtimePrice of(StockCode stockCode, String rawContent) {
        List<String> items = Configs.COMMA_SPLITTER.splitToList(rawContent);
        String id = stockCode.code();
        DataType type = DataType.StockPrice;
        LocalDateTime localDateTime =
                Utils.toLocalDateTime(String.format("%s %s", items.get(30), items.get(31)));

        StockRealtimePrice stockRealtimePrice =
                new StockRealtimePrice(id, type, localDateTime);
        stockRealtimePrice.stockCode(stockCode.code()).stockName(stockCode.name())
                .open(Double.valueOf(items.get(1)))
                .close(Double.valueOf(items.get(2)))
                .current(Double.valueOf(items.get(3)))
                .highest(Double.valueOf(items.get(4)))
                .lowest(Double.valueOf(items.get(5)))
                .volume(Long.valueOf(items.get(8)))
                .amount(Double.valueOf(items.get(9)))
                .bidVolume1(Long.valueOf(items.get(10)))
                .bidPrice1(Double.valueOf(items.get(11)))
                .bidVolume2(Long.valueOf(items.get(12)))
                .bidPrice2(Double.valueOf(items.get(13)))
                .bidVolume3(Long.valueOf(items.get(14)))
                .bidPrice3(Double.valueOf(items.get(15)))
                .bidVolume4(Long.valueOf(items.get(16)))
                .bidPrice4(Double.valueOf(items.get(17)))
                .bidVolume5(Long.valueOf(items.get(18)))
                .bidPrice5(Double.valueOf(items.get(19)))
                .askVolume1(Long.valueOf(items.get(20)))
                .askPrice1(Double.valueOf(items.get(21)))
                .askVolume2(Long.valueOf(items.get(22)))
                .askPrice2(Double.valueOf(items.get(23)))
                .askVolume3(Long.valueOf(items.get(24)))
                .askPrice3(Double.valueOf(items.get(25)))
                .askVolume4(Long.valueOf(items.get(26)))
                .askPrice4(Double.valueOf(items.get(27)))
                .askVolume5(Long.valueOf(items.get(28)))
                .askPrice5(Double.valueOf(items.get(29)));

        return stockRealtimePrice;
    }

    @Tag
    private String stockCode;
    @Tag
    private String stockName;

    @SeriesProperty(description = "开盘价")
    private double open;

    @SeriesProperty(description = "收盘价")
    private double close;

    @SeriesProperty(description = "当前价")
    private double current;

    @SeriesProperty(description = " 最高价")
    private double highest;

    @SeriesProperty(description = "最低价")
    private double lowest;

    @SeriesProperty(description = "成交量")
    private long volume;

    @SeriesProperty
    private double amount;

    @SeriesProperty
    private long bidVolume1;

    @SeriesProperty
    private double bidPrice1;

    @SeriesProperty
    private long bidVolume2;

    @SeriesProperty
    private double bidPrice2;

    @SeriesProperty
    private long bidVolume3;

    @SeriesProperty
    private double bidPrice3;

    @SeriesProperty
    private long bidVolume4;

    @SeriesProperty
    private double bidPrice4;

    @SeriesProperty
    private long bidVolume5;

    @SeriesProperty
    private double bidPrice5;

    @SeriesProperty
    private long askVolume1;

    @SeriesProperty
    private double askPrice1;

    @SeriesProperty
    private long askVolume2;

    @SeriesProperty
    private double askPrice2;

    @SeriesProperty
    private long askVolume3;

    @SeriesProperty
    private double askPrice3;

    @SeriesProperty
    private long askVolume4;

    @SeriesProperty
    private double askPrice4;

    @SeriesProperty
    private long askVolume5;

    @SeriesProperty
    private double askPrice5;

    public StockRealtimePrice(String id, DataType dataType,
                              LocalDateTime dateTime) {
        super(id, dataType, dateTime);
    }

    @Override
    protected Map<String, String> generateTags() {
        return null;
    }
}
