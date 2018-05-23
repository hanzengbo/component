package tv.xiaoka.router.compiler.constant;

/**
 * Created by zengbo1 on 2018/5/22.
 */

public interface Constant {
    String KEY_HOST_NAME = "host";
    String ANNO_PKG = "tv.xiaoka.router.annotation.annonation";
    String ANNOTATION_TYPE_ROUTE_NODE = ANNO_PKG + ".RouteNode";

    String PREFIX_OF_LOGGER = "[Router-Anno-Compiler]-- ";

    // System interface
    String ACTIVITY = "android.app.Activity";
    String FRAGMENT = "android.app.Fragment";
    String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    String SERVICE = "android.app.Service";
    String PARCELABLE = "android.os.Parcelable";

    // Java type
    String LANG = "java.lang";
    String BYTE = LANG + ".Byte";
    String SHORT = LANG + ".Short";
    String INTEGER = LANG + ".Integer";
    String LONG = LANG + ".Long";
    String FLOAT = LANG + ".Float";
    String DOUBEL = LANG + ".Double";
    String BOOLEAN = LANG + ".Boolean";
    String STRING = LANG + ".String";

    String BASECOMPROUTER = "tv.xiaoka.componentlib.router.ui.BaseCompRouter";
}
