package org.xmuyoo.blueberry.collect.utils.id.generator;

public interface VersionedSchema {

    int lowBit();

    long serviceCodeMusk();

    long serviceCodeLeft();

    long extraMusk();

    long extraLeft();

    long clusterMusk();

    long clusterLeft();

    long descriptorMusk();

    long descriptorLeft();

    long preserveMusk();

    long timestampMusk();

    long timestampLeft();

    long sequenceMusk();

    long maxServiceCode();

    long maxExtra();

    long maxTimestamp();

    long maxSequence();
}
