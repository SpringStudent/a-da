package io.github.springstudent.ada.client.utils;

import static io.github.springstudent.ada.client.utils.UnitUtilities.toByteSize;
import static java.lang.String.format;

public final class SystemUtilities {

    private SystemUtilities() {
    }

    public static String getRamInfo() {
        final double freeMG = Runtime.getRuntime().freeMemory();
        final double totalMG = Runtime.getRuntime().totalMemory();
        return format("%s of %s", toByteSize(totalMG - freeMG), toByteSize(totalMG));
    }

}
