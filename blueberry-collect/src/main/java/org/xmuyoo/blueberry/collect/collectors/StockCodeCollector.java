package org.xmuyoo.blueberry.collect.collectors;

import lombok.extern.slf4j.Slf4j;
import org.xmuyoo.blueberry.collect.domains.DataSchema;
import org.xmuyoo.blueberry.collect.domains.StockCode;
import org.xmuyoo.blueberry.collect.http.HttpClient;
import org.xmuyoo.blueberry.collect.storage.PgClient;
import org.xmuyoo.blueberry.collect.storage.ValueType;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class StockCodeCollector extends BasicCollector {

    private final TShareData tShareData;

    public StockCodeCollector(PgClient meta, HttpClient http) {
        super("stock-code", meta);

        this.tShareData = new TShareData(http);
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected boolean collect() {
        List<StockCode> codeList = tShareData.stockCodeList();
        codeList.forEach(c -> System.out.println(c.toString()));
        this.storage.saveIgnoreDuplicated(codeList, StockCode.class);

        return true;
    }

    @Override
    protected List<DataSchema> getDataSchemaList() {
        return toSchemaList(StockCode.class);
    }
}
