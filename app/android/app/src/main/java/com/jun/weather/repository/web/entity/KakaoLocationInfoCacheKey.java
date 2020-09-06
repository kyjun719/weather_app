package com.jun.weather.repository.web.entity;

import androidx.annotation.Nullable;

public class KakaoLocationInfoCacheKey {
    public String urlPath;
    public double x,y;

    @Override
    public int hashCode() {
        int ret = 17;
        ret = ret*31+urlPath.hashCode();
        ret = ret*31+Double.valueOf(x).hashCode();
        ret = ret*31+Double.valueOf(y).hashCode();

        return ret;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof KakaoLocationInfoCacheKey) {
            KakaoLocationInfoCacheKey key = (KakaoLocationInfoCacheKey) obj;
            return key.urlPath.equals(this.urlPath) &&
                    key.x == this.x &&
                    key.y == this.y;
        }
        return false;
    }
}

