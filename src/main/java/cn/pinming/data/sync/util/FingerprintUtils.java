package cn.pinming.data.sync.util;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.ClassUtils;

import javax.activation.UnsupportedDataTypeException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 对象指纹工具
 */
public class FingerprintUtils {
    /**
     * 获取对象指纹值
     *
     * @param obj   数据对象
     * @param group 字段分组
     * @return MD5签名值
     */
    public static String getFingerprint(Object obj, Class<?> group, boolean ignoreEmptyValue) {
        Class<?> clz = obj.getClass();
        // 找到需要生成指纹的字段
        List<Field> fieldsToResolve = findFieldToSign(clz, group);
        // 将字段解析
        List<SignField> resolvedFieldsToSign = resolveFields(obj, fieldsToResolve, ignoreEmptyValue, group);
        System.out.println(JSONObject.toJSONString(resolvedFieldsToSign));
        // 将解析后的字段拼成字符串
        String strToSign = buildStringToSign(resolvedFieldsToSign);
        System.out.println(strToSign);
        // 将字符串进行MD5签名 生成指纹数据
        return getFingerprint(strToSign);
    }

    private static String getFingerprint(String data) {
        return DigestUtils.md5Hex(data);
    }


    private static String buildStringToSign(List<SignField> resolvedFields) {
        resolvedFields.sort(Comparator.comparing(SignField::getFieldName));
        StringBuilder strToSign = new StringBuilder();
        for (SignField resolvedField : resolvedFields) {
            strToSign.append(resolvedField.toString()).append("&");
        }
        String buildString = strToSign.toString();
        if (buildString.length() > 0) {
            buildString = buildString.substring(0, strToSign.length() - 1);
        }
        return buildString;
    }


    private static List<SignField> resolveFields(Object obj, List<Field> fields, boolean ignoreEmptyValue, Class<?> group) {
        return SignField.fromFields(null,fields, obj, ignoreEmptyValue, group);
    }

    private static List<Field> findFieldToSign(Class<?> clz, Class<?> group) {
        Field[] fields = clz.getDeclaredFields();
        List<Field> resolvedFild = new ArrayList<>();
        for (Field field : fields) {
            FingerprintField fingerField = field.getAnnotation(FingerprintField.class);
            if (fingerField == null) continue;
            Class<?>[] fieldGroups = fingerField.groups();
            // 未指定group, 不验证已经标记了groups字段的验证
            if (group == null) {
                if (fieldGroups.length == 0) {
                    resolvedFild.add(field);
                }
            } else {
                if (Arrays.asList(fieldGroups).contains(group)) {
                    resolvedFild.add(field);
                }
            }
        }
        return resolvedFild;
    }


    @Getter
    @Setter
    private static class SignField {
        private String fieldName;
        private Object fieldValue;

        @Override
        public String toString() {
            return fieldName + "=" + fieldValue;
        }

        public static List<SignField> fromFields(String parentName, List<Field> fields, Object obj, boolean ignoreEmptyValue, Class<?> group) {
            List<SignField> sfs = new LinkedList<>();
            for (Field field : fields) {
                String fieldName = StringUtils.isBlank(parentName) ? field.getName() : parentName + "." + field.getName();
                boolean accessable = field.isAccessible();
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    //ignore
                }
                field.setAccessible(accessable);
                if (ignoreEmptyValue) {
                    if (value == null) continue;
                    if ((value instanceof String) && StringUtils.isBlank((String) value)) continue;
                    if ((value instanceof List) && ((List) value).isEmpty()) continue;
                    if ((value instanceof Map) && ((Map) value).isEmpty()) continue;
                }
                if (value instanceof List) {
                    List<SignField> childs = getListFields(fieldName, (List<?>) value, ignoreEmptyValue, group);
                    sfs.addAll(childs);
                } else if (value instanceof Map) {
                    throw new RuntimeException(new UnsupportedDataTypeException(value.getClass().getName()));
                } else if (value == null || ClassUtils.isPrimitive(value.getClass())) {
                    SignField sf = new SignField();
                    sf.setFieldName(fieldName);
                    sf.setFieldValue(value);
                    sfs.add(sf);
                } else {
                    List<Field> fs = findFieldToSign(value.getClass(), group);
                    List<SignField> childs = fromFields(fieldName, fs, value, ignoreEmptyValue, group);
                    sfs.addAll(childs);
                }
            }
            return sfs;
        }

        private static List<SignField> getListFields(String name, List<?> value, boolean ignoreEmptyValue, Class<?> group) {
            List<SignField> sfs = new ArrayList<>();
            for (int i = 0; i < value.size(); i++) {
                Object o = value.get(i);
                if (o == null) continue;
                String n = name + "[" + i + "]";
                if (ClassUtils.isPrimitive(o.getClass())) {
                    SignField sf = new SignField();
                    sf.setFieldName(n);
                    sf.setFieldValue(value);
                    sfs.add(sf);
                } else if (o instanceof List) {
                    List<SignField> childFields = getListFields(n, (List<?>) o, ignoreEmptyValue, group);
                    sfs.addAll(childFields);
                } else if (o instanceof Map) {
                    throw new RuntimeException(new UnsupportedDataTypeException(value.getClass().getName()));
                } else {
                    List<Field> ffs = findFieldToSign(o.getClass(), group);
                    List<SignField> fs = fromFields(n, ffs, o, ignoreEmptyValue, group);
                    sfs.addAll(fs);
                }
            }
            return sfs;
        }
    }


}
