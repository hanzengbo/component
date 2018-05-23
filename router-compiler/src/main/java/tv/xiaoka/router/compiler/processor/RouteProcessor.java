package tv.xiaoka.router.compiler.processor;

import static javax.lang.model.element.Modifier.PUBLIC;
import static tv.xiaoka.router.compiler.constant.Constant.BASECOMPROUTER;
import static tv.xiaoka.router.compiler.constant.Constant.KEY_HOST_NAME;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import tv.xiaoka.router.annotation.annonation.Autowired;
import tv.xiaoka.router.annotation.annonation.RouteNode;
import tv.xiaoka.router.annotation.enums.NodeType;
import tv.xiaoka.router.annotation.model.Node;
import tv.xiaoka.router.annotation.utils.RouteUtils;
import tv.xiaoka.router.compiler.constant.Constant;
import tv.xiaoka.router.compiler.utils.Logger;
import tv.xiaoka.router.compiler.utils.StringUtils;
import tv.xiaoka.router.compiler.utils.TypeUtils;

@AutoService(Processor.class)
@SupportedOptions(KEY_HOST_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Constant.ANNOTATION_TYPE_ROUTE_NODE})
public class RouteProcessor extends AbstractProcessor {
    private static final String mRouteMapperFieldName = "routeMapper";
    private static final String mParamsMapperFieldName = "paramsMapper";

    private Logger logger;
    private String host = null;
    private Elements elements;
    private Filer mFiler;
    private Types types;
    private TypeUtils typeUtils;
    private ArrayList<Node> routerNodes;
    private TypeMirror type_String;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        routerNodes = new ArrayList<>();
        elements = processingEnv.getElementUtils();
        type_String = elements.getTypeElement("java.lang.String").asType();
        mFiler = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        typeUtils = new TypeUtils(types, elements);
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && options.size() > 0) {
            host = options.get(KEY_HOST_NAME);
            logger.info(">>> host is " + host + " <<<");
        }
        if (host == null || host.equals("")) {
            host = "default";
        }
        logger.info(">>> RouteProcessor init. <<<" + host);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        logger.info( "start: --------------");
        if (set != null && set.size() > 0) {
            for (TypeElement typeElement : set) {
                logger.info( "set = " + typeElement);
            }
            Set<? extends Element> routeNodes = roundEnvironment.getElementsAnnotatedWith(RouteNode.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                parseRouteNodes(routeNodes);
            } catch (Exception e) {
                logger.error(e);
            }
            generateRouterImpl();
        }
        return true;
    }

    /**
     *
     * @param routeElements
     */
    private void parseRouteNodes(Set<? extends Element> routeElements) {
        TypeMirror type_Activity = elements.getTypeElement(Constant.ACTIVITY).asType();
        for (Element element : routeElements) {
            //获取注解类名
            TypeMirror tm = element.asType();
            if (types.isSubtype(tm, type_Activity)) {
                //获取注解信息
                RouteNode route = element.getAnnotation(RouteNode.class);
                String path = route.path();
                logger.info("tm = " + tm + " path = " + path);
                checkPath(path);
                Node node = new Node();
                node.setPath(path);
                node.setDesc(route.desc());
                node.setPriority(route.priority());
                node.setNodeType(NodeType.ACTIVITY);
                node.setRawType(element);

                Map<String, Integer> paramsType = new HashMap<>();
                Map<String, String> paramsDesc = new HashMap<>();
                for (Element field : element.getEnclosedElements()) {
                    if (field.getKind().isField() && field.getAnnotation(Autowired.class) != null) {
                        Autowired paramConfig = field.getAnnotation(Autowired.class);
                        logger.info("paramConfig = " + paramConfig.name() + " typedesc = " + typeUtils.typeDesc(field));
                        paramsType.put(StringUtils.isEmpty(paramConfig.name())
                                ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeExchange(field));
                        paramsDesc.put(StringUtils.isEmpty(paramConfig.name())
                                ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeDesc(field));
                    }
                }
                node.setParamsType(paramsType);
                node.setParamsDesc(paramsDesc);
                if (!routerNodes.contains(node)) {
                    routerNodes.add(node);
                }
            } else {
                throw new IllegalStateException("only activity can be annotated by RouteNode");
            }

        }
    }

    /**
     * generate HostUIRouter.java
     */
    private void generateRouterImpl() {
        String claName = RouteUtils.genHostUIRouterClass(host);
        //pkg
        String pkg = claName.substring(0, claName.lastIndexOf("."));
        //simpleName
        String cn = claName.substring(claName.lastIndexOf(".") + 1);
        // superClassName
        ClassName superClass = ClassName.get(elements.getTypeElement(BASECOMPROUTER));
        MethodSpec initHostMethod = generateInitHostMethod();
        MethodSpec initMapMethod = generateInitMapMethod();

        try {
            JavaFile.builder(pkg, TypeSpec.classBuilder(cn)
                    .addModifiers(PUBLIC)
                    .superclass(superClass)
                    .addMethod(initHostMethod)
                    .addMethod(initMapMethod)
                    .build()
            ).build().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * create init host method
     */
    private MethodSpec generateInitHostMethod() {
        TypeName returnType = TypeName.get(type_String);

        MethodSpec.Builder openUriMethodSpecBuilder = MethodSpec.methodBuilder("getHost")
                .returns(returnType)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        openUriMethodSpecBuilder.addStatement("return $S", host);

        return openUriMethodSpecBuilder.build();
    }

    /**
     * create init map method
     */
    private MethodSpec generateInitMapMethod() {
        TypeName returnType = TypeName.VOID;

        MethodSpec.Builder openUriMethodSpecBuilder = MethodSpec.methodBuilder("initMap")
                .returns(returnType)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        openUriMethodSpecBuilder.addStatement("super.initMap()");

        for (Node node : routerNodes) {
            openUriMethodSpecBuilder.addStatement(
                    mRouteMapperFieldName + ".put($S,$T.class)",
                    node.getPath(),
                    ClassName.get((TypeElement) node.getRawType()));

            // Make map body for paramsType
            StringBuilder mapBodyBuilder = new StringBuilder();
            Map<String, Integer> paramsType = node.getParamsType();
            if (paramsType != null && paramsType.size() > 0) {
                for (Map.Entry<String, Integer> types : paramsType.entrySet()) {
                    mapBodyBuilder.append("put(\"").append(types.getKey()).append("\", ").append(types.getValue()).append("); ");
                }
            }
            String mapBody = mapBodyBuilder.toString();
            logger.info(">>> mapBody: " + mapBody + " <<<");
            if (!StringUtils.isEmpty(mapBody)) {
                openUriMethodSpecBuilder.addStatement(
                        mParamsMapperFieldName + ".put($T.class,"
                                + "new java.util.HashMap<String, Integer>(){{" + mapBody + "}}" + ")",
                        ClassName.get((TypeElement) node.getRawType()));
            }
        }

        return openUriMethodSpecBuilder.build();
    }

    private void checkPath(String path) {
        if (path == null || path.isEmpty() || !path.startsWith("/"))
            throw new IllegalArgumentException("path cannot be null or empty,and should start with /,this is:" + path);

        if (path.contains("//") || path.contains("&") || path.contains("?"))
            throw new IllegalArgumentException("path should not contain // ,& or ?,this is:" + path);

        if (path.endsWith("/"))
            throw new IllegalArgumentException("path should not endWith /,this is:" + path
                    + ";or append a token:index");
    }
}
