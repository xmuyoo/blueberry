package org.xmuyoo.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterValidator {

    public static List<String> notBlank(Map<String, Object> body, String... parameters) {
        List<String> required = new ArrayList<>();
        for (String parameter : parameters) {
            if (body.containsKey(parameter))
                continue;

            required.add(parameter);
        }

        return required;
    }

    public static boolean notBlank(Object... parameters) {
        for (Object parameter : parameters) {
            if (null == parameter)
                return false;

            if (parameter instanceof String) {
                if (((String) parameter).isEmpty())
                    return false;
            }
        }

        return true;
    }
}
