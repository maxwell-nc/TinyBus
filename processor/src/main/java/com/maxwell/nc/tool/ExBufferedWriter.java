package com.maxwell.nc.tool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 扩展BufferedWriter
 */
public class ExBufferedWriter extends BufferedWriter {

    public ExBufferedWriter(Writer writer) {
        super(writer);
    }

    public void writeLn(String s) throws IOException {
        write(s + "\n");
    }

    public void writeLn(String s, Object... args) throws IOException {
        write(String.format(s, args) + "\n");
    }

    /**
     * 写入一个空行
     */
    public void writeLn() throws IOException {
        write("\n");
    }

}