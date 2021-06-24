
package com.davi.tinker.study.build.gradle.util;

import java.util.Collection;


public class Utils {

    public static boolean isNullOrNil(final String object) {
        return (object == null) || (object.length() <= 0);
    }

    public static boolean isNullOrNil(final Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

}
