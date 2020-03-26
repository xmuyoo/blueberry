package org.xmuyoo.storage.repos;


import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLangDriver extends XMLLanguageDriver implements LanguageDriver {

    private final Pattern FOREACH_PATTERN = Pattern.compile("foreach: #\\{([\\w\\d_]+)\\}");


    /**
     * 实现自定义 Select 注解
     *
     * @param configuration 配置参数
     * @param script        入参
     * @param parameterType 参数类型
     * @return 转换后的 SqlSource
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {

        Matcher matcher = FOREACH_PATTERN.matcher(script);
        if (matcher.find()) {
            script = matcher.replaceAll(
                    " <foreach collection=\"$1\" item=\"_item\" open=\"(\" "
                            + "separator=\",\" close=\")\" >#{_item}</foreach> ");
        }

        script = "<script>" + script + "</script>";

        return super.createSqlSource(configuration, script, parameterType);
    }
}
