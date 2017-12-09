package com.dianping.pigeon.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author qi.yin
 *         2016/06/05  下午6:02.
 */
public class GZipCompress implements Compress {

    /**
     * default buffer size
     */
    private static final int BUFFER_SIZE = 256;

    public byte[] compress(byte[] array) throws IOException {
        if (array == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(array);
            gzip.finish();
            gzip.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (gzip != null) {
                gzip.close();
            }
        }
        return out.toByteArray();
    }

    public byte[] unCompress(byte[] array) throws IOException {
        if (array == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(array);
        GZIPInputStream zip = null;
        try {
            zip = new GZIPInputStream(in);
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = zip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
        return out.toByteArray();
    }

}
