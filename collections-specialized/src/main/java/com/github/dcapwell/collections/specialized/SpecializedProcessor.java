package com.github.dcapwell.collections.specialized;

import com.github.dcapwell.collections.specialized.Specialized.Type;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class SpecializedProcessor extends AbstractProcessor {
    private static final ImmutableMap<Type, TypeKind> NUM_TO_TYPE =
            ImmutableMap.<Type, TypeKind>builder()
                    .put(Type.BYTE, TypeKind.BYTE)
                    .put(Type.SHORT, TypeKind.SHORT)
                    .put(Type.INT, TypeKind.INT)
                    .put(Type.LONG, TypeKind.LONG)
                    .put(Type.FLOAT, TypeKind.FLOAT)
                    .put(Type.DOUBLE, TypeKind.DOUBLE)
                    .build();
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Specialized.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(Specialized.class)) {
            if (e.getKind() != ElementKind.CLASS && e.getKind() != ElementKind.INTERFACE) {
                printError(e);
                return true;
            }
            // get required types
            TypeElement outterClass = (TypeElement) e;
            Specialized specialized = outterClass.getAnnotation(Specialized.class);
            List<? extends TypeParameterElement> typeParameters = outterClass.getTypeParameters();
            int tpSize = typeParameters.size();
            Set<Type>[] paramTypes = new Set[tpSize];
            for (int i = 0, size = tpSize; i < size; i++)
                paramTypes[i] = Sets.newEnumSet(Arrays.asList(specialized.types()), Type.class);
            for (List<Type> group : Sets.cartesianProduct(Arrays.asList(paramTypes))) {
                LinkedHashMap<TypeMirror, TypeKind> sub = Maps.newLinkedHashMapWithExpectedSize(tpSize);
                for (int i = 0; i < group.size(); i++)
                    sub.put(typeParameters.get(i).asType(), NUM_TO_TYPE.get(group.get(i)));

                try {
                    JavaFile javaFile = createSource(outterClass, sub);
                    JavaFileObject file = filer.createSourceFile(javaFile.packageName + "." + javaFile.typeSpec.name);
                    try (Writer writer = file.openWriter()) {
                        javaFile.writeTo(writer);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return true;
    }

    private JavaFile createSource(TypeElement e, LinkedHashMap<TypeMirror, TypeKind> typeSub) {
        String name = createClassName(e, typeSub);
        TypeSpec.Builder spec = e.getKind() == ElementKind.CLASS? TypeSpec.classBuilder(name) : TypeSpec.interfaceBuilder(name);
        spec.addModifiers(e.getModifiers().toArray(new Modifier[0]));
        for (Element elem : e.getEnclosedElements()) {
            switch (elem.getKind()) {
                case CONSTRUCTOR:
                case METHOD:
                    spec.addMethod(createMethod((ExecutableElement) elem, typeSub));
                    break;
            }
        }
        JavaFile javaFile = JavaFile.builder(e.getQualifiedName().toString().replace("." + e.getSimpleName(), ""), spec.build())
                .build();
        return javaFile;
    }

    private static String createClassName(TypeElement e, LinkedHashMap<TypeMirror, TypeKind> typeSub) {
        StringBuilder prefix = new StringBuilder();
        typeSub.values().forEach(n -> prefix.append(className(n)));
        return prefix.toString() + e.getSimpleName();
    }

    private static String className(TypeKind typeKind) {
        switch (typeKind) {
            case BYTE: return "Byte";
            case SHORT: return "Short";
            case INT: return "Int";
            case LONG: return "Long";
            case FLOAT: return "Float";
            case DOUBLE: return "Double";
            default: throw new IllegalArgumentException("Unknown type: " + typeKind);
        }
    }

    private MethodSpec createMethod(ExecutableElement method, LinkedHashMap<TypeMirror, TypeKind> typeSub) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());
        methodBuilder.addModifiers(method.getModifiers().toArray(new Modifier[0]));
        if (method.getKind() != ElementKind.CONSTRUCTOR)
            methodBuilder.returns(TypeName.get(replace(method.getReturnType(), typeSub, method)));

        for (VariableElement parameter : method.getParameters()) {
            TypeName type = TypeName.get(replace(parameter.asType(), typeSub, parameter));
            String name = parameter.getSimpleName().toString();
            Set<Modifier> parameterModifiers = parameter.getModifiers();
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name)
                    .addModifiers(parameterModifiers.toArray(new Modifier[parameterModifiers.size()]));
            for (AnnotationMirror mirror : parameter.getAnnotationMirrors()) {
                parameterBuilder.addAnnotation(AnnotationSpec.get(mirror));
            }
            methodBuilder.addParameter(parameterBuilder.build());
        }
        methodBuilder.varargs(method.isVarArgs());

        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }
        methodBuilder.addTypeVariables(method.getTypeParameters().stream().<TypeVariableName>map(TypeVariableName::get).collect(Collectors.toList()));
        return methodBuilder.build();
    }

    private TypeMirror replace(TypeMirror mirror, LinkedHashMap<TypeMirror, TypeKind> typeSub, Element parameter) {
        switch (mirror.getKind()) {
            case ARRAY: return typeUtils.getArrayType(replace(((ArrayType) mirror).getComponentType(), typeSub, parameter));
            case DECLARED: {
                DeclaredType dt = (DeclaredType) mirror;
                List<TypeMirror> replaced = new ArrayList<>(dt.getTypeArguments().size());
                List<TypeMirror> remaining = new ArrayList<>();
                for (TypeMirror t : dt.getTypeArguments()) {
                    TypeMirror r = replace(t, typeSub, parameter);
                    if (typeUtils.isSameType(t, r)) {
                        // got the same type back
                        remaining.add(t);
                    } else {
                        replaced.add(r);
                    }
                }
                if (replaced.isEmpty()) {
                    // nothing was modified, return the type unchanged
                    return mirror;
                } else {
                    //TODO switch to ClassName since the element doesn't exist yet (we are creating it!)
                    // need to Specialize the type
                    TypeElement e = (TypeElement) dt.asElement();
                    String name = createClassName(e, typeSub);
                    PackageElement packageName = elementUtils.getPackageOf(e);
                    String fullElementName = packageName + "." + name;
                    e = elementUtils.getTypeElement(fullElementName);
                    if (e == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to locate type " + fullElementName + "; returning type " + mirror, parameter);
                        return mirror;
                    }
                    return typeUtils.getDeclaredType(e, remaining.toArray(new TypeMirror[0]));
                }
            }
            case TYPEVAR:
                // this will work for methods that get their variables from the class
                // static methods that infer their own methods won't match
                if (typeSub.containsKey(mirror))
                    return typeUtils.getPrimitiveType(typeSub.get(mirror));
                messager.printMessage(Diagnostic.Kind.ERROR, "Unknown mirror type: " + mirror + "; expected " + typeSub.keySet(), parameter);
                return mirror;
        }
        return mirror;
    }

    private void printError(Element outerClass) {
        messager.printMessage(Diagnostic.Kind.ERROR,
                String.format("@%s can only be used for a class", Specialized.class.getCanonicalName()),
                outerClass);
    }
}