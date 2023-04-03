package fit.wenchao.http_file_server.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
public class ClassUtils {

    public static <T> T getFieldValue(Object obj, String fieldName, Class<T> fieldType) throws NoSuchFieldException {
        Field declaredField = null;
        Class<?> targetClass = obj.getClass();
        try {
            declaredField = targetClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw e;
        }
        Class<?> fieldActualClass = declaredField.getType();
        if (!fieldType.isAssignableFrom(fieldActualClass)) {
            log.error("指定的字段类型:{}与实际属性的类型：{}不匹配", fieldType, fieldActualClass);
            throw new RuntimeException("指定的字段类型与实际属性的类型不匹配");
        }
        if (declaredField != null) {
            declaredField.setAccessible(true);
            T o = null;
            try {
                o = (T) declaredField.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return o;
        }
        return null;
    }

    private static String getMethodTypeString(Method declaredMethod) {
        if (declaredMethod.getParameterCount() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
        for (Class<?> paramType : parameterTypes) {
            sb.append(paramType.getTypeName() + "-");
        }


        String substring = sb.substring(0, sb.length() - 1);

        return substring;

    }

}
