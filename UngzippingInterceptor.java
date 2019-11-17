
import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UngzippingInterceptor implements Interceptor {
    private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

    private static final String HEADER_GZIP = "gzip";

    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return this.ungzip(response);
    }

    private Response ungzip(Response response) throws IOException {
        if (response.body() != null && this.isGzipped(response)) {

            try{

                InputStream inputFormtGZIP = maybeDecompress(response.body().source().inputStream());

                String data = StringGZipper.ungzip(readBytes(inputFormtGZIP));

                MediaType contentType = response.body().contentType();
                ResponseBody body = ResponseBody.create(contentType, data);

                return response.newBuilder().body(body).build();

            }catch (Exception err){
                return response;
            }

        } else {
            return response;
        }
    }

    private boolean isGzipped(Response response) {
        Headers headers = response.headers();

        for (int i = 0; i < headers.size(); ++i) {
            String name = headers.name(i);
            String value = headers.value(i);
            if (HEADER_CONTENT_ENCODING.equalsIgnoreCase(name) && HEADER_GZIP.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGZIPStream(byte[] bytes)  {
        return bytes[0] == (byte) GZIPInputStream.GZIP_MAGIC
                && bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> 8);
    }

    private enum StringGZipper {
        ;
        private static String ungzip(byte[] bytes) throws IOException{
            InputStreamReader isr = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(bytes)), StandardCharsets.UTF_8);
            StringWriter sw = new StringWriter();
            char[] chars = new char[bytes.length];
            for (int len; (len = isr.read(chars)) > 0; ) {
                sw.write(chars, 0, len);
            }
            return sw.toString();
        }

        private static byte[] gzip(String s) throws IOException{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            OutputStreamWriter osw = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
            osw.write(s);
            osw.close();
            return bos.toByteArray();
        }
    }

    private static byte[] readBytes( InputStream stream ) throws IOException {
        if (stream == null) return new byte[] {};
        byte[] buffer = new byte[stream.available()];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean error = false;
        try {
            int numRead = 0;
            while ((numRead = stream.read(buffer)) > -1) {
                output.write(buffer, 0, numRead);
            }
        } catch (IOException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } catch (RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                if (!error) throw e;
            }
        }
        output.flush();
        return output.toByteArray();
    }

    private static InputStream maybeDecompress(InputStream input)  throws IOException  {
        final PushbackInputStream pb = new PushbackInputStream(input, 2);

        int header = pb.read();
        if(header == -1) {
            return pb;
        }

        int b = pb.read();
        if(b == -1) {
            pb.unread(header);
            return pb;
        }

        pb.unread(new byte[]{(byte)header, (byte)b});

        header = (b << 8) | header;

        if(header == GZIPInputStream.GZIP_MAGIC) {
            return new GZIPInputStream(pb);
        } else {
            return pb;
        }
    }
}




