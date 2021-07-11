package com.jun.weather.repository.web.entity;

import java.util.List;

public class RestResponse<T> {
    public int code;
    public String failMsg;
    public List<T> listBody;
    public T singleBody;
}
