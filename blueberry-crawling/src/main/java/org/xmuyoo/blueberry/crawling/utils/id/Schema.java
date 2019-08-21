package org.xmuyoo.blueberry.crawling.utils.id;

public class Schema {

    public static VersionedSchema VERSION_0 = new V0();

    private static final long VERSION_BIT = 4L;
    public static final long VERSION_MUSK = ~(-1L << VERSION_BIT);
    public static final long VERSION_LEFT = 59L;

    public static VersionedSchema versionedSchema(long version) {
        switch (Long.valueOf(version).intValue()) {
            case V0.VERSION_CODE:
                return VERSION_0;
            default:
                return VERSION_0;
        }
    }

    private static class V0 implements VersionedSchema {
        private static final int VERSION_CODE = 0;

        private static final long SERVICE_BIT = 20L;
        private static final long EXTRA_BIT = 6L;
        private static final long CLUSTER_BIT = 5L;
        private static final long DESCRIPTOR_BIT = 5L;
        private static final long PRESERVE_BIT = 23L;
        private static final long TIMESTAMP_BIT = 33L;
        private static final long SEQUENCE_BIT = 30L;
        private static final long MAX_SERVICE_CODE = (1L << SERVICE_BIT) - 1;
        private static final long MAX_EXTRA = (1L << EXTRA_BIT) - 1;
        private static final long MAX_TIMESTAMP = (1L << TIMESTAMP_BIT) - 1;
        private static final long MAX_SEQUENCE = (1L << SEQUENCE_BIT) - 1;
        private static final int LOW_BIT = 64;

        private static final long SERVICE_MUSK = ~(-1L << SERVICE_BIT);
        private static final long EXTRA_MUSK = ~(-1L << EXTRA_BIT);
        private static final long CLUSTER_MUSK = ~(-1L << CLUSTER_BIT);
        private static final long DESCRIPTOR_MUSK = ~(-1L << DESCRIPTOR_BIT);
        private static final long PRESERVE_MUSK = ~(-1L << PRESERVE_BIT);

        private static final long DESCRIPTOR_LEFT = PRESERVE_BIT;
        private static final long CLUSTER_LEFT = DESCRIPTOR_BIT + DESCRIPTOR_LEFT;
        private static final long EXTRA_LEFT = CLUSTER_BIT + CLUSTER_LEFT;
        private static final long SERVICE_LEFT = EXTRA_BIT + EXTRA_LEFT;

        private static final long TIMESTAMP_MUSK = ~(-1L << TIMESTAMP_BIT);
        private static final long SEQUENCE_MUSK = ~(-1L << SEQUENCE_BIT);
        private static final long TIMESTAMP_LEFT = SEQUENCE_BIT;

        @Override
        public int lowBit() {
            return V0.LOW_BIT;
        }

        @Override
        public long serviceCodeMusk() {
            return V0.SERVICE_MUSK;
        }

        @Override
        public long serviceCodeLeft() {
            return V0.SERVICE_LEFT;
        }

        @Override
        public long extraMusk() {
            return V0.EXTRA_MUSK;
        }

        @Override
        public long extraLeft() {
            return V0.EXTRA_LEFT;
        }

        @Override
        public long clusterMusk() {
            return V0.CLUSTER_MUSK;
        }

        @Override
        public long clusterLeft() {
            return V0.CLUSTER_LEFT;
        }

        @Override
        public long descriptorMusk() {
            return V0.DESCRIPTOR_MUSK;
        }

        @Override
        public long descriptorLeft() {
            return V0.DESCRIPTOR_LEFT;
        }

        @Override
        public long preserveMusk() {
            return V0.PRESERVE_MUSK;
        }

        @Override
        public long timestampMusk() {
            return V0.TIMESTAMP_MUSK;
        }

        @Override
        public long timestampLeft() {
            return V0.TIMESTAMP_LEFT;
        }

        @Override
        public long sequenceMusk() {
            return V0.SEQUENCE_MUSK;
        }

        @Override
        public long maxServiceCode() {
            return V0.MAX_SERVICE_CODE;
        }

        @Override
        public long maxExtra() {
            return V0.MAX_EXTRA;
        }

        @Override
        public long maxTimestamp() {
            return V0.MAX_TIMESTAMP;
        }

        @Override
        public long maxSequence() {
            return V0.MAX_SEQUENCE;
        }
    }
}
