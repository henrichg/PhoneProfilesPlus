package sk.henrichg.phoneprofilesplus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class InputStreamUtil {

    public InputStreamUtil() {
    }

    static String read(InputStream is) {
        try {
            char[] buffer = new char[1024];
            StringBuilder out = new StringBuilder();
            InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8);

            int read;
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.append(buffer, 0, read);
                }
            } while(read >= 0);

            return out.toString();
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }
    }

    @SuppressWarnings("unused")
    static void copy(InputStream is, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];

            int read;
            do {
                read = is.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.write(buffer, 0, read);
                }
            } while(read >= 0);

        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }
}
