package org.xmuyoo.domains;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "driver"})})
@ToString
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Crawler {

    public static final Crawler StockRealtimePriceCrawler =
            new Crawler(1L, "StockRealtimePrice",
                        "org.xmuyoo.blueberry.crawling.crawlers.StockRealtimePriceCrawler",
                        "股票实时交易数据爬虫，只在开市时间段内爬取每只股票的实时数据");
    @Id
    private Long id;

    @JsonProperty
    @Column(columnDefinition = "text not null")
    private String name;

    @JsonProperty
    @Column(columnDefinition = "text not null")
    private String driver;

    @JsonProperty
    @Column(columnDefinition = "text")
    private String description;
}
