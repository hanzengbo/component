package tv.xiaoka.router.compiler;

import static tv.xiaoka.router.compiler.constant.Constant.KEY_HOST_NAME;

import com.google.auto.service.AutoService;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
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

import tv.xiaoka.router.annotation.annonation.RouteNode;
import tv.xiaoka.router.compiler.constant.Constant;
import tv.xiaoka.router.compiler.utils.Logger;

@AutoService(Processor.class)
@SupportedOptions(KEY_HOST_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Constant.ANNOTATION_TYPE_ROUTE_NODE})
public class RouteProcessor extends AbstractProcessor {

    private Logger logger;
    private String host = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

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

    private void parseRouteNodes(Set<? extends Element> routeElements) {
        //TypeMirror type_Activity = elements.getTypeElement(Constant.ACTIVITY).asType();
        for (Element element : routeElements) {
            //获取注解类名
            TypeMirror tm = element.asType();
            RouteNode route = element.getAnnotation(RouteNode.class);
            String path = route.path();
            logger.info("tm = " + tm + " path = " + path);
        }
    }
}
