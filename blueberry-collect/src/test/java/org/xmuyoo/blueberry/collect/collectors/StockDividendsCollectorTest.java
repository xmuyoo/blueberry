package org.xmuyoo.blueberry.collect.collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmuyoo.blueberry.collect.domains.StockDividends;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockDividendsCollectorTest {

    private static final String SAMPLE_HTML = ""
            + "<div class=\"page-loading\" style=\"display: none;\">"
            + "    <div class=\"mask\"></div>"
            + "    <div class=\"loading-img\"></div>"
            + "</div>"
            + "<table class=\"m-table J-ajax-table J-canvas-table\">"
            + "    <thead>"
            + "        <tr class=\"row2\">"
            + "            <th>序号</th>"
            + "            <th>股票代码</th>"
            + "            <th>股票简称</th>"
            + "            <th>最新价</th>"
            + "            <th>预计<br/>除权除息价</th>"
            + "            <th>是否<br/>已分配</th>"
            + "            <th>送股<br/>(每十股)</th>"
            + "            <th>转增股<br/>(每十股)</th>"
            + "            <th>送转总数<br/>(每十股)</th>"
            + "            <th>派息/元<br/>(每十股)</th>"
            + "            <th>公告披露日</th>"
            + "            <th>股权登记日</th>"
            + "            <th>除权除息日</th>"
            + "        </tr>"
            + "    </thead>"
            + "    <tbody>"
            + "        <tr>"
            + "            <td>1</td>"
            + "            <td class=\"tc\"><a class=\"stockCode\">601166</a></td>"
            + "            <td class=\"linkToGghq\"><a>兴业银行</a></td>"
            + "            <td class=\"tc\">18.55</td>"
            + "            <td class=\"tc\">18.05</td>"
            + "            <td class=\"tc\">否</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">5.01</td>"
            + "            <td>2026-03-27</td>"
            + "            <td class=\"tc\">--</td>"
            + "            <td class=\"tc\">--</td>"
            + "        </tr>"
            + "        <tr class=\"even\">"
            + "            <td>2</td>"
            + "            <td class=\"tc\"><a class=\"stockCode\">601166</a></td>"
            + "            <td class=\"linkToGghq\"><a>兴业银行</a></td>"
            + "            <td class=\"tc\">18.55</td>"
            + "            <td class=\"tc\">-</td>"
            + "            <td class=\"tc\">是</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">5.65</td>"
            + "            <td>2025-10-31</td>"
            + "            <td class=\"tc\">2026-02-05</td>"
            + "            <td class=\"tc\">2026-02-06</td>"
            + "        </tr>"
            + "        <tr>"
            + "            <td>3</td>"
            + "            <td class=\"tc\"><a class=\"stockCode\">601166</a></td>"
            + "            <td class=\"linkToGghq\"><a>兴业银行</a></td>"
            + "            <td class=\"tc\">18.55</td>"
            + "            <td class=\"tc\">17.80</td>"
            + "            <td class=\"tc\">否</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">--</td>"
            + "            <td class=\"tr\">10.60</td>"
            + "            <td>2025-03-28</td>"
            + "            <td class=\"tc\">2025-06-19</td>"
            + "            <td class=\"tc\">2025-06-20</td>"
            + "        </tr>"
            + "    </tbody>"
            + "</table>";

    private StockDividendsCollector collector;

    @BeforeEach
    void setUp() {
        // parseHtml is package-private, can be tested without full collector setup
        // Pass null for PgClient/ChClient/HttpClient since we only test parsing
        collector = new StockDividendsCollector(null, null, null);
    }

    @Test
    void shouldSkipRowsWithoutExDividendDate() {
        List<StockDividends> result = collector.parseHtml("601166", "兴业银行", SAMPLE_HTML);

        // Row 1: date="--" → skipped
        // Row 2: price="-" → skipped (no valid price)
        // Row 3: price=17.80, date=2025-06-20 → kept
        assertEquals(1, result.size());
    }

    @Test
    void shouldExtractCodeAndNameFromHtml() {
        List<StockDividends> result = collector.parseHtml("601166", "兴业银行", SAMPLE_HTML);

        for (StockDividends d : result) {
            assertEquals("601166", d.code());
            assertEquals("兴业银行", d.name());
        }
    }

    @Test
    void shouldParsePriceCorrectly() {
        List<StockDividends> result = collector.parseHtml("601166", "兴业银行", SAMPLE_HTML);

        // Only row 3 kept, price=17.80
        assertEquals(17.80, result.get(0).price(), 0.001);
    }

    @Test
    void shouldParseDateCorrectly() {
        List<StockDividends> result = collector.parseHtml("601166", "兴业银行", SAMPLE_HTML);

        // Row 3: 除权除息日 = 2025-06-20
        assertEquals("2025-06-20", result.get(0).recordDate());
    }

    @Test
    void shouldNotMatchHeaderRow() {
        List<StockDividends> result = collector.parseHtml("601166", "兴业银行", SAMPLE_HTML);

        // Header row has <th>, not <td>, so it should not be included
        // The result should contain only data rows
        assertEquals(1, result.size(), "Should only contain 1 row with valid price and date");
    }

    @Test
    void shouldHandleNumericPriceCorrectly() {
        // Row 1: price=18.05 but date="--" → excluded
        // Row 2: price="-" → excluded (no valid price)
        // Row 3: price=17.80 and date=2025-06-20 → kept
        List<StockDividends> result = collector.parseHtml("601166", "兴业银行", SAMPLE_HTML);

        assertEquals(1, result.size());

        // Row 1 with price=18.05 excluded because date is "--"
        boolean hasRow18 = result.stream()
                .anyMatch(d -> d.price() != null && d.price() == 18.05);
        assertFalse(hasRow18, "Row with price=18.05 and date=-- should be excluded");

        // Row 3 with price=17.80 included
        assertEquals(17.80, result.get(0).price(), 0.001);
        assertEquals("2025-06-20", result.get(0).recordDate());
    }
}
