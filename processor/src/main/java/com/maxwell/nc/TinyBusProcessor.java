package com.maxwell.nc;

import com.maxwell.nc.tool.ExBufferedWriter;
import com.maxwell.nc.tool.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({TinyBusProcessor.EVENT_CLASS_NAME})
public class TinyBusProcessor extends AbstractProcessor {

    static final String EVENT_CLASS_NAME = "com.maxwell.nc.library.annotation.TinyEvent";

    private LogUtils logUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        logUtils = new LogUtils(processingEnv.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (set == null) {
            logUtils.showLog("没有需要处理的注解！");
            return true;
        }

        //key:类 values:方法列表
        Map<Element, List<ExecutableElement>> subscribeMethodMap = new HashMap<>();

        for (TypeElement annotateType : set) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotateType);
            for (Element method : elements) {//获取每一个方法

                if (!(method instanceof ExecutableElement)) {
                    logUtils.showError("注解只能用于方法上！");
                    return false;
                }

                ExecutableElement executableMethod = (ExecutableElement) method;

                if (executableMethod.getModifiers().contains(Modifier.STATIC)) {
                    logUtils.showError("订阅方法不能为static！");
                    return false;
                }

                if (!executableMethod.getModifiers().contains(Modifier.PUBLIC)) {
                    logUtils.showError("订阅方法应该为public！");
                    return false;
                }

                //获取方法参数
                List<? extends VariableElement> parameters = executableMethod.getParameters();
                if (parameters.size() != 1) {
                    logUtils.showError("订阅方法必须有且仅有一个参数！");
                    return false;
                }

                //拿到注解所在的类
                Element annotateClass = executableMethod.getEnclosingElement();

                //按类名归类每个注解方法
                List<ExecutableElement> elementList = subscribeMethodMap.get(annotateClass);
                if (elementList == null) {
                    elementList = new ArrayList<>();
                }
                elementList.add(executableMethod);
                subscribeMethodMap.put(annotateClass, elementList);
            }
        }

        for (Map.Entry<Element, List<ExecutableElement>> methodEntry : subscribeMethodMap.entrySet()) {
            try {
                writeJava(methodEntry.getKey(), methodEntry.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void writeJava(Element classElement, List<ExecutableElement> methodList) throws Exception {
        String buildClassName = classElement.getSimpleName().toString() + "$$BusSubscriber";
        String fullClassName = classElement.toString();

        //获取包名，适配内部类
        Element packageElement = classElement.getEnclosingElement();
        while (!(packageElement instanceof PackageElement)) {
            packageElement = packageElement.getEnclosingElement();
        }
        String packageName = ((PackageElement) packageElement).getQualifiedName().toString();

        //创建对应Java文件
        JavaFileObject sourceFile = processingEnv.getFiler()
                .createSourceFile(buildClassName);
        ExBufferedWriter writer = new ExBufferedWriter(sourceFile.openWriter());
        writer.writeLn("package %s;", packageName);
        writer.writeLn();
        writer.writeLn("import com.maxwell.nc.library.EventListener;");
        writer.writeLn("import com.maxwell.nc.library.ReflectSubscriber;");
        writer.writeLn("import com.maxwell.nc.library.TinyBus;");
        writer.writeLn("import com.maxwell.nc.library.core.TinyBusSubscriber;");
        writer.writeLn();
        writer.writeLn("/** This class is generated by TinyBus !!!DO NOT EDIT!!! */");
        writer.writeLn("public class %s<T extends %s> implements ReflectSubscriber<T> {", buildClassName, fullClassName);
        writer.writeLn();

        for (ExecutableElement method : methodList) {
            String subscriberPrefix = getSubscriberFiledName(method);
            logUtils.showLog("generate " + subscriberPrefix);
            writer.writeLn("    private TinyBusSubscriber %s;", subscriberPrefix);
        }

        writer.writeLn();
        writer.writeLn("    @Override");
        writer.writeLn("    public void register(final %s source) {", fullClassName);

        for (ExecutableElement method : methodList) {
            String methodName = method.getSimpleName().toString();

            //获取方法第一个参数类型
            VariableElement variableElement = method.getParameters().get(0);
            TypeMirror typeMirror = variableElement.asType();

            //获取注解参数
            String thread = "null";
            String isSticky = "false";

            List<? extends AnnotationMirror> annotationMirrors = method.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                if (EVENT_CLASS_NAME.equals(annotationMirror.getAnnotationType().toString())) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                        if ("thread".equals(entry.getKey().getSimpleName().toString())) {
                            thread = entry.getValue().toString();
                        }
                        if ("sticky".equals(entry.getKey().getSimpleName().toString())) {
                            isSticky = entry.getValue().toString();
                        }
                    }
                }
            }

            writer.writeLn("        %s = TinyBus.getDefault().subscribeEvent(\"%s\", %s, %s,", getSubscriberFiledName(method), typeMirror, thread, isSticky);
            writer.writeLn("                new EventListener<%s>() {", typeMirror);
            writer.writeLn("                    @Override");
            writer.writeLn("                    public void onEvent(%s event) {", typeMirror);
            writer.writeLn("                        source.%s(event);", methodName);
            writer.writeLn("                    }");
            writer.writeLn("                });");
        }

        writer.writeLn("    }");
        writer.writeLn();
        writer.writeLn("    @Override");
        writer.writeLn("    public void unRegister() {");

        for (ExecutableElement method : methodList) {
            writer.writeLn("        TinyBus.getDefault().unSubscribeEvent(%s);", getSubscriberFiledName(method));
        }

        writer.writeLn("    }");
        writer.writeLn();
        writer.write("}");

        writer.close();
    }

    /**
     * 获取生成的订阅者成员变量名
     */
    private String getSubscriberFiledName(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        VariableElement variableElement = method.getParameters().get(0);//获取方法第一个参数
        String argClassName = variableElement.asType().toString();
        char[] argArray = argClassName.toCharArray();
        for (int i = -1; i < argArray.length - 1; i++) {
            if (i == -1 || argArray[i] == '.') {
                int next = i + 1;
                if (next < argArray.length) {
                    //.字符下一个字母转换为大写
                    if (argArray[next] >= 'a' && argArray[next] <= 'z') {
                        argArray[next] = (char) (argArray[next] - 32);
                    }
                }
            }
        }
        return methodName + new String(argArray).replace(".", "") + "Subscriber";
    }

}
