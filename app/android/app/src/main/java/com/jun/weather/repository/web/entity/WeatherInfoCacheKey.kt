package com.jun.weather.repository.web.entity;

import androidx.annotation.Nullable;

public class WeatherInfoCacheKey {
    public int key;
    public int nx,ny;
    public String baseDate, baseTime;
    public String midDate, regionId;

    @Override
    public int hashCode() {
        int ret = 17;
        ret = ret*31+key;
        ret = ret*31+nx;
        ret = ret*31+ny;
        ret = ret*31+baseDate.hashCode();
        ret = ret*31+baseTime.hashCode();
        ret = ret*31+midDate.hashCode();
        ret = ret*31+regionId.hashCode();
        return ret;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof WeatherInfoCacheKey) {
            WeatherInfoCacheKey o = (WeatherInfoCacheKey) obj;
            return (o.baseTime.equals(this.baseTime)) &&
                    (o.baseDate.equals(this.baseDate)) &&
                    (o.nx == this.nx) && (o.ny == this.ny) &&
                    (o.key == this.key) &&
                    (o.midDate.equals(this.midDate)) &&
                    (o.regionId.equals(this.regionId));
        }
        return false;
    }
}
