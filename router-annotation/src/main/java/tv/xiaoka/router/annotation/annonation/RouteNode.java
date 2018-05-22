package tv.xiaoka.router.annotation.annonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zengbo1 on 2018/5/22.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface RouteNode {
    /**
     * path of one route
     */
    String path();
    /**
     * The priority of route.
     */
    int priority() default -1;

    /**
     * description of the activity, user for gen route table
     */
    String desc() default "";
}
