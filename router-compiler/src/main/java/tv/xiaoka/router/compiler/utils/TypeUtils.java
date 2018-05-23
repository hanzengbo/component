package tv.xiaoka.router.compiler.utils;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import tv.xiaoka.router.annotation.enums.Type;
import tv.xiaoka.router.compiler.constant.Constant;

/**
 * <p><b>Package:</b> com.luojilab.router.compiler.utils </p>
 * <p><b>Project:</b> DDComponentForAndroid </p>
 * <p><b>Classname:</b> TypeUtils </p>
 * <p><b>Description:</b> utils for type inference </p>
 * Created by leobert on 2017/9/18.
 */

public class TypeUtils {
    private Types types;
    private Elements elements;
    private TypeMirror parcelableType;

    public TypeUtils(Types types, Elements elements) {
        this.types = types;
        this.elements = elements;

        parcelableType = this.elements.getTypeElement(Constant.PARCELABLE).asType();
    }

    /**
     * Diagnostics out the true java type
     *
     * @param element Raw type
     * @return Type class of java
     */
    public int typeExchange(Element element) {
        TypeMirror typeMirror = element.asType();

        // Primitive
        if (typeMirror.getKind().isPrimitive()) {
            return element.asType().getKind().ordinal();
        }

        switch (typeMirror.toString()) {
            case Constant.BYTE:
                return Type.BYTE.ordinal();
            case Constant.SHORT:
                return Type.SHORT.ordinal();
            case Constant.INTEGER:
                return Type.INT.ordinal();
            case Constant.LONG:
                return Type.LONG.ordinal();
            case Constant.FLOAT:
                return Type.FLOAT.ordinal();
            case Constant.DOUBEL:
                return Type.DOUBLE.ordinal();
            case Constant.BOOLEAN:
                return Type.BOOLEAN.ordinal();
            case Constant.STRING:
                return Type.STRING.ordinal();
            default:    // Other side, maybe the PARCELABLE or OBJECT.
                if (types.isSubtype(typeMirror, parcelableType)) {  // PARCELABLE
                    return Type.PARCELABLE.ordinal();
                } else {    // For others
                    return Type.OBJECT.ordinal();
                }
        }
    }

    /**
     * DESC of type
     *
     * @param element Raw type
     * @return Type class of java
     */
    public String typeDesc(Element element) {
        TypeMirror typeMirror = element.asType();

        // Primitive
        if (typeMirror.getKind().isPrimitive()) {
            return element.asType().getKind().name();
        }

        switch (typeMirror.toString()) {
            case Constant.BYTE:
                return "byte";
            case Constant.SHORT:
                return "short";
            case Constant.INTEGER:
                return "int";
            case Constant.LONG:
                return "long";
            case Constant.FLOAT:
                return "byte";
            case Constant.DOUBEL:
                return "double";
            case Constant.BOOLEAN:
                return "boolean";
            case Constant.STRING:
                return "String";
            default:    // Other side, maybe the PARCELABLE or OBJECT.
                if (types.isSubtype(typeMirror, parcelableType)) {  // PARCELABLE
                    return "parcelable";
                } else {    // For others
                    return typeMirror.toString();
                }
        }
    }


}
