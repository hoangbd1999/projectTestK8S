package com.elcom.adminconsolebackend.dto;

import com.elcom.adminconsolebackend.util.JsonUtils;
import com.elcom.adminconsolebackend.util.StreamUtils;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ValueWrapper<T> {

    private T value;

    public static class ListValueWrapper<U> extends ValueWrapper<List<U>> { }

    public static <U> ListValueWrapper<U> listInstanceOf(Class<U> clazz, String data) {
        if (LocalDateTime.class != clazz) {
            ListValueWrapper<U> typeRetriever = new ListValueWrapper<>();
            return JsonUtils.fromJson(data, typeRetriever.getClass());
        } else {
            ListValueWrapper<String> typeRetriever = new ListValueWrapper<>();
            ListValueWrapper<String> tempWrappedValue = JsonUtils.fromJson(data, typeRetriever.getClass());

            List<LocalDateTime> value = StreamUtils.toList(tempWrappedValue.getValue(), DateUtils::parse);
            ListValueWrapper<LocalDateTime> wrappedValue = new ListValueWrapper<>();
            wrappedValue.setValue(value);
            return (ListValueWrapper<U>) wrappedValue;
        }
    }

}
