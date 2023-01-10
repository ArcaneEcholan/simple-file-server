package fit.wenchao.http_file_server.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class ClassUtils {

    /**
     * @param jarEntries
     * @param packageName
     * @param isRecursion
     * @return
     */
    private static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<String>();

        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory()) {
                /*
                 * 这里是为了方便，先把"/" 转成 "." 再判�? ".class" 的做法可能会有bug
                 * (FIXME: 先把"/" 转成 "." 再判�? ".class" 的做法可能会有bug)
                 */
                String entryName = jarEntry.getName().replace("/", ".");
                log.debug("处理jar entry：{}", entryName);
                if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
                    entryName = entryName.replace(".class", "");
                    if (isRecursion) {
                        classNames.add(entryName);
                    } else if (!entryName.replace(packageName + ".", "").contains(".")) {
                        classNames.add(entryName);
                    }
                }
            }else {
                log.debug("处理jar 目录：{}", jarEntry);

            }
        }

        return classNames;
    }

    /**
     * 从项目文件获取某包下�?有类
     *
     * @param filePath    文件路径
     * @param packageName 包名
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion, Predicate<String> predicate) {
        Set<String> className = new HashSet<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        for (File childFile : files) {
            //�?查一个对象是否是文件�?
            if (childFile.isDirectory()) {
                if (isRecursion) {
                    className.addAll(getClassNameFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion, predicate));
                }
            } else {
                String fileName = childFile.getName();
                //endsWith() 方法用于测试字符串是否以指定的后�?结束�?  !fileName.contains("$") 文件名中不包�? '$'
                String fullClassName = packageName + "." + fileName.replace(".class", "");

                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    if (predicate != null && predicate.test(fullClassName)) {
                        className.add(fullClassName);
                    }

                }
            }

        }

        return className;
    }


    /**
     * 获取某包下所有类
     *
     * @param packageName 包名
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    public static Set<String> getClassName(String packageName, boolean isRecursion, Predicate<String> predicate) {
        Set<String> classNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        log.debug("目标类包路径：{}", packagePath);
        URL url = loader.getResource(packagePath);
        if (url != null) {
            log.debug("资源找到");
            String protocol = url.getProtocol();
            log.debug("资源protocol：{}", protocol);
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion, predicate);
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                try {
                    log.debug("打开jar");
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (jarFile != null) {
                    log.debug("jar打开成功");
                    getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            log.debug("资源未找到");
        }
        // else {
        //    /*从所有的jar包中查找包名*/
        //    classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        //}
        log.info("结果类名集合：{}", classNames);
        return classNames;
    }

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

    private static String getMethodSig(Method method) {
        return method.getName() + "-" + getMethodTypeString(method);
    }

    public static String getMethodSig(HandlerMethod method) {
        return getMethodSig(method.getMethod());
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

    public static Map<Method, String> getAllControllerMethods(ApplicationContext run) {
        Map<Method, String> mapSig = new HashMap<>();
        List<String> result = new ArrayList<>();
        //获取restcontroller注解的类名
        String[] beanNamesForAnnotation = run.getBeanNamesForAnnotation(RestController.class);
        for (String str : beanNamesForAnnotation) {
            Object bean = run.getBean(str);
            Class<?> forName = bean.getClass();

            Method[] declaredMethods = forName.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                String className = forName.getName();
                String sig = getMethodSig(declaredMethod);
                className += "-";
                className += sig;
                mapSig.put(declaredMethod, className);
                result.add(className);
            }
        }
        return mapSig;

    }

    public static <T extends Annotation> T getFieldAnnotation(Class<T> annotation, Class target) {
        Field[] declaredFields = target.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            T annotation1 = declaredField.getAnnotation(annotation);
            if (annotation1 != null) {
                return annotation1;
            }
        }
        return null;
    }

    public static <T extends Annotation> Field getFieldAnnotatedBy(Class<T> annotation, Class target) {
        Field[] declaredFields = target.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            T annotation1 = declaredField.getAnnotation(annotation);
            if (annotation1 != null) {
                return declaredField;
            }
        }
        return null;
    }
}
