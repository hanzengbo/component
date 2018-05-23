package tv.xiaoka.router.compiler;

import static tv.xiaoka.router.compiler.constant.Constant.KEY_HOST_NAME;

import com.google.auto.service.AutoService;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import tv.xiaoka.router.annotation.annonation.Autowired;
import tv.xiaoka.router.annotation.annonation.RouteNode;
import tv.xiaoka.router.annotation.enums.NodeType;
import tv.xiaoka.router.annotation.model.Node;
import tv.xiaoka.router.compiler.constant.Constant;
import tv.xiaoka.router.compiler.utils.Logger;
import tv.xiaoka.router.compiler.utils.StringUtils;
import tv.xiaoka.router.compiler.utils.TypeUtils;

@AutoService(Processor.class)
@SupportedOptions(KEY_HOST_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Constant.ANNOTATION_TYPE_ROUTE_NODE})
public class RouteProcessor extends AbstractProcessor {

    private Logger logger;
    private String host = null;
    private Elements elements;
    private Filer mFiler;
    private Types types;
    private TypeUtils typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elements = processingEnv.getElementUtils();
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
                        paramsType.put(StringUtils.isEmpty(paramConfig.name())
                                ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeExchange(field));
                        paramsDesc.put(StringUtils.isEmpty(paramConfig.name())
                                ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeDesc(field));
                    }
                }
            }
        }
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
