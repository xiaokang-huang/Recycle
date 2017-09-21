package com.jiagu.utils;

import java.io.FileDescriptor;

public class serialUtil {
	public static native FileDescriptor uartOpen(String port, int baudrate, int flags);
	public static native void uartClose(FileDescriptor file);

	static {
		System.loadLibrary("jiaguutils");
	}

	public static String dumpByteArray(byte[] data, int offset, int len) {
		StringBuilder sb = new StringBuilder(String.format("(%d)[", len));
		for (int i = 0; i < len; ++i) {
			sb.append(String.format("%x,", data[i + offset]));
		}
		sb.append("]");
		return sb.toString();
	}
}