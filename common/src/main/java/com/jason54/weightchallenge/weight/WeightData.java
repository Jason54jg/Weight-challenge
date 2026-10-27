package com.jason54.weightchallenge.weight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;

/**
 * Données de poids attachées à un joueur (sérialisées via attachements loader).
 */
public final class WeightData {
    public static final MapCodec<WeightData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("WeightKg").forGetter(WeightData::getWeightKg)
    ).apply(instance, WeightData::new));

    public static final Codec<WeightData> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<FriendlyByteBuf, WeightData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            WeightData::getWeightKg,
            WeightData::new
    );
    private double weightKg;

    public WeightData(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("WeightKg", weightKg);
        return tag;
    }

    public static WeightData fromTag(CompoundTag tag) {
        return new WeightData(tag.getDoubleOr("WeightKg", 450.0D));
    }

    public WeightData copy() {
        return new WeightData(weightKg);
    }
}
