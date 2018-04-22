package com.maxwell.nc.tool;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * 打印日志工具类
 */
public class LogUtils {

    private Messager envPrinter;

    public LogUtils(Messager envPrinter) {
        this.envPrinter = envPrinter;
    }

    /**
     * 显示日志
     */
    public void showLog(Object object) {
        if (envPrinter != null) {
            envPrinter.printMessage(Diagnostic.Kind.NOTE, "TinyBus: " + object.toString());
        }
    }

    /**
     * 显示日志
     */
    public void showError(Object object) {
        if (envPrinter != null) {
            envPrinter.printMessage(Diagnostic.Kind.ERROR, "TinyBus: " + object.toString());
        }
    }

}
