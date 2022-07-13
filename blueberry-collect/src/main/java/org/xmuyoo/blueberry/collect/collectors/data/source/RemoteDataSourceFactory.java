package org.xmuyoo.blueberry.collect.collectors.data.source;

public class RemoteDataSourceFactory {

    private static final RemoteDataSourceFactory DATA_SOURCE_FACTORY = new RemoteDataSourceFactory();

    private RemoteDataSource JISILU = new Jisilu();

    public enum DataSourceType {
        Jisilu,
    }

    public static RemoteDataSource getDataSource(DataSourceType dataSourceType) {
        switch (dataSourceType) {
            case Jisilu:
                return DATA_SOURCE_FACTORY.JISILU;
            default:
                throw new IllegalArgumentException("Unsupported remote data source type: " + dataSourceType.name());
        }
    }
}
