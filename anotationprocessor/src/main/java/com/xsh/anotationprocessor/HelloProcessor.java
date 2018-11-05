package com.xsh.anotationprocessor;

import com.google.auto.service.AutoService;
import com.xsh.adapter.Adapter;
import com.xsh.adapter.Holder;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class HelloProcessor extends AbstractProcessor {
    Messager messager;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "HelloProcessor init " + processingEnv.getOptions());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new LinkedHashSet<>();
        supportedAnnotationTypes.add(Adapter.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        warn("HelloProcessor process2" + roundEnv.getRootElements());
        warn("process3" + annotations);

        StringBuilder initalize = new StringBuilder();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Adapter.class);
        for (Element element :
                elements) {
            TypeElement typeElement = (TypeElement) element;
            warn(typeElement.getQualifiedName() + "  " + typeElement.getAnnotationMirrors());
            warn(".......");

            JSONObject jsonObject = new JSONObject();
            String _cls = null;

            Adapter holder = element.getAnnotation(Adapter.class);
            int s = holder.layout();
            try {
                Class names = holder.holder();
                warn("3" + names);
                _cls = names.getCanonicalName();
            } catch (MirroredTypeException e) {
                TypeMirror typeMirror = e.getTypeMirror();
                String className = typeMirror.toString();
                warn(className + "  " + typeMirror.getKind() + "  " + typeMirror.getClass().getCanonicalName());
                _cls = className;
            }

            try {
                jsonObject.put("layout", holder.layout());
                jsonObject.put("holder", _cls);
            } catch (Exception e) {

            }


            if (initalize.length() == 0)
                initalize.append("String");
            initalize.append(String.format("\tstr = \"%s\" ;", jsonObject.toString().replace("\"", "\\\"")));
            initalize.append("\n\t");
            initalize.append(String.format("data.put(\"%s\",str);\n", typeElement.getQualifiedName()));

//
//            List<? extends AnnotationMirror> annotationMirrorList = typeElement.getAnnotationMirrors();
//            for (AnnotationMirror mirror :
//                    annotationMirrorList) {
//                String str = mirror.getAnnotationType().toString();
//                TypeElement typeMirror = (TypeElement) mirror.getAnnotationType().asElement();
//                str = typeMirror.getQualifiedName().toString();
//                warn(str);
//                switch (str) {
//                    case "com.xsh.hello.Adapter":
//                        Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirror.getElementValues();
//                        Class values = null;
//                        int layout = 0;
//                        String key = null;
//                        for (ExecutableElement executableElement :
//                                map.keySet()) {
//                            switch (executableElement.getSimpleName().toString()) {
//
//                                case "values":
//                                    // holder = (Class)map.get(executableElement).getValue();
//                                    break;
//                                case "layout":
//                                    layout = (int) map.get(executableElement).getValue();
//                                    break;
//
//                            }
//                        }
//
//
//                        warn("Holder注解参数格式正确");
//                        warn("创建getItemViewType");
//                        MethodSpec getItemViewType = MethodSpec.methodBuilder("getItemViewType")
//                                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
//                                .returns(int.class)
//                                .addParameter(int.class, "position")
//                                .addCode("return 0;\n")
//                                .build();
//
//
//                        warn("创建onCreateViewHolder ");
//                        MethodSpec onCreateViewHolder = MethodSpec.methodBuilder("onCreateViewHolder")
//                                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
//                                .returns(RecyclerView.ViewHolder.class)
//                                .addParameter(ViewGroup.class, "parent")
//                                .addParameter(int.class, "viewType")
//                                .addCode("$T recyclerItem=null;\n", View.class)
//                                .addCode("recyclerItem= $T.from(parent.getContext()).inflate(" + layout + ",null);\n", LayoutInflater.class)
//                                .addCode("return new $T(recyclerItem);\n", DefaultHolder.class)
//                                .build();
//
//
//                        warn("创建getItemCount ");
//                        MethodSpec getItemCount = MethodSpec.methodBuilder("getItemCount")
//                                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
//                                .returns(int.class)
//                                .addCode("return 0;\n")
//                                .build();
//
//
//                        warn("创建onBindViewHolder ");
//                        MethodSpec onBindViewHolder = MethodSpec.methodBuilder("onBindViewHolder")
//                                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
//                                .returns(void.class)
//                                .addParameter(RecyclerView.ViewHolder.class, "viewHolder")
//                                .addParameter(int.class, "position")
//                                .build();
//
//
//                        warn("创建adapter");
//                        TypeSpec adapter = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "_")
//                                .superclass(RecyclerView.Adapter.class)
//                                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
//                                .addMethod(getItemViewType)
//                                .addMethod(onCreateViewHolder)
//                                .addMethod(getItemCount)
//                                .addMethod(onBindViewHolder)
//                                .build();
//
//                        warn("创建adapterJavaFile");
//                        JavaFile adapterJavaFile = JavaFile.builder(((PackageElement) typeElement.getEnclosingElement()).getQualifiedName().toString(), adapter)
//                                .build();
//
//                        try {
//                            String fileName = adapterJavaFile.packageName.isEmpty()
//                                    ? adapterJavaFile.typeSpec.name
//                                    : adapterJavaFile.packageName + "." + adapterJavaFile.typeSpec.name;
//                            File tmp = new File(fileName);
//                            if (tmp.exists())
//                                tmp.delete();
//                            adapterJavaFile.writeTo(processingEnv.getFiler());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        warn("创建Ａdapter完成");
//
//                        break;
//                }
//            }
        }
        warn("map:" + processingEnv.getOptions().toString());

        if (initalize.length() > 0) {
            try {
                String code = String.format("package com.xsh;\n" +
                        "\n" +
                        "\n" +
                        "import java.util.HashMap;\n" +
                        "import java.util.Map;\n" +
                        "\n" +
                        "public class BaseAdapter{\n" +
                        "  \n" +
                        "  public Map<String,String>  data = new HashMap<>();\n" +
                        "\n" +
                        "  public BaseAdapter() {\n" +
                        "    %s" +
                        "  }\n" +
                        "}", initalize.toString());
                JavaFileObject file = processingEnv.getFiler().createSourceFile("com.xsh.BaseAdapter");
                Writer writer = file.openWriter();
                writer.write(code);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /********************************解析Holder注解************************************/
        Set<? extends Element> holderSet = roundEnv.getElementsAnnotatedWith(Holder.class);
        for (Element element :
                holderSet) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                String variableType = variableElement.asType().toString();
                if (!variableElement.getModifiers().toString().contains("static")) {
                    messager.printMessage(Diagnostic.Kind.ERROR, variableElement.getSimpleName() + "变量没有static关键字");
                }
                if (!variableType.equals("java.lang.Object[][]")) {
                    messager.printMessage(Diagnostic.Kind.ERROR, variableElement.getSimpleName() + "变量不是java.lang.Object[][]类型");
                }
                warn(element.getSimpleName() + "  " + variableType + "  " + variableElement.getModifiers());
            }
        }


        //
        //  messager.printMessage(Diagnostic.Kind.ERROR, "fdfdf");
        return true;
    }

    public void warn(CharSequence text) {
        messager.printMessage(Diagnostic.Kind.WARNING, text);
    }

}