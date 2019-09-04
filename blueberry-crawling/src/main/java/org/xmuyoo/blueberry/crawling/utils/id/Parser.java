package org.xmuyoo.blueberry.crawling.utils.id;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Map;

public class Parser {

    public enum Info {
        SERVICE,
        EXTRA,
        TIMESTAMP,
        SEQUENCE,
        DESCRIPTOR
    }

    static final VersionedSchema CURRENT_SCHEMA = Schema.VERSION_0;

    public static final int WHOLE_ID = 0;
    public static final int TWO_LONG = 1;
    public static final int RADIX = 2;

    public static final int RADIX_36 = 36;

    public static final long DEFAULT_CLUSTER = 0L;
    private static final long BEGINNING = 0L;

    public static Serializer serialize(long svcCode, long ext, long desc, long ts, long seq) {

        Serializer serializer = Serializer.builder().serviceCode(svcCode).extra(ext)
                .descriptor(desc).timestamp(ts).sequence(seq).build();
        serializer.serialize();

        return serializer;
    }

    public static Map<Info, Number> parseFromNormal(String id) {
        BigInteger value = new BigInteger(id);

        int lowBit = CURRENT_SCHEMA.lowBit();
        BigInteger low = value.subtract(value.shiftRight(lowBit).shiftLeft(lowBit));
        BigInteger high = value.shiftRight(lowBit);
        return parseFromParts(high.longValue(), low.longValue());

    }

    public static Map<Info, Number> parserFromRadix(String radixId) {
        BigInteger value = new BigInteger(radixId, RADIX_36);
        return parseFromNormal(value.toString());
    }

    public static Map<Info, Number> parseFromParts(long high, long low) {

        Deserializer deserializer = new Deserializer(high, low);

        return ImmutableMap.of(
                Info.SERVICE, deserializer.serviceCode(), Info.EXTRA, deserializer.extra(),
                Info.TIMESTAMP, deserializer.timestamp(), Info.SEQUENCE,
                deserializer.sequence(), Info.DESCRIPTOR, deserializer.descriptor());
    }

    public static boolean isValid(long serviceCode, long extra, long ts, long seq) {
        return validServiceCode(serviceCode)
                && validExtra(extra)
                && validSequence(seq)
                && validTimestamp(ts);
    }

    private static boolean validTimestamp(long timestamp) {
        return BEGINNING < timestamp && timestamp <= CURRENT_SCHEMA.maxTimestamp();
    }

    public static boolean validServiceCode(long serviceCode) {
        return BEGINNING <= serviceCode && serviceCode <= CURRENT_SCHEMA.maxServiceCode();
    }

    public static boolean validExtra(long extra) {
        return BEGINNING <= extra && extra <= CURRENT_SCHEMA.maxExtra();
    }

    public static boolean validSequence(long seq) {
        return BEGINNING <= seq && seq <= CURRENT_SCHEMA.maxSequence();
    }

    @Getter
    static class Deserializer {

        private long high;
        private long low;
        private long version;
        private long serviceCode;
        private long extra;
        private long timestamp;
        private long sequence;
        private long descriptor;
        private long cluster;
        private long preserve;


        Deserializer(long high, long low) {
            this.high = high;
            this.low = low;

            version = (high >> Schema.VERSION_LEFT) & Schema.VERSION_MUSK;
            VersionedSchema versionedSchema = Schema.versionedSchema(version);

            serviceCode = (high >> versionedSchema.serviceCodeLeft()) & versionedSchema
                    .serviceCodeMusk();
            extra = (high >> versionedSchema.extraLeft()) & versionedSchema.extraMusk();
            descriptor = (high >> versionedSchema.descriptorLeft()) & versionedSchema
                    .descriptorMusk();
            cluster = (high >> versionedSchema.clusterLeft()) & versionedSchema.clusterMusk();
            preserve = high & versionedSchema.preserveMusk();

            timestamp = (low >> versionedSchema.timestampLeft()) & versionedSchema.timestampMusk();
            sequence = low & versionedSchema.sequenceMusk();
        }
    }

    @Builder
    public static class Serializer {


        @Getter
        private long high;
        @Getter
        private long low;

        // high space
        private long version;
        private long serviceCode;
        private long extra;
        private long descriptor;
        private long cluster = DEFAULT_CLUSTER;
        private long preserve;

        // low space
        private long timestamp;
        private long sequence;

        private void serialize() {
            // high
            high = serviceCode << CURRENT_SCHEMA.serviceCodeLeft()
                    | extra << CURRENT_SCHEMA.extraLeft()
                    | descriptor << CURRENT_SCHEMA.descriptorLeft()
                    | cluster << CURRENT_SCHEMA.clusterLeft();

            // low
            low = timestamp << CURRENT_SCHEMA.timestampLeft() | sequence;
        }

        private BigInteger toBigInteger() {
            BigInteger highInteger = BigInteger.valueOf(high);
            BigInteger lowInteger = BigInteger.valueOf(low);

            return highInteger.shiftLeft(CURRENT_SCHEMA.lowBit()).or(lowInteger);
        }

        @Override
        public String toString() {
            BigInteger integer = toBigInteger();
            return integer.toString();
        }

        public String toString(int radix) {
            BigInteger integer = toBigInteger();
            return integer.toString(radix);
        }
    }

}
