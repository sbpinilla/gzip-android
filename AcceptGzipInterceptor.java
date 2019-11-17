package co.com.pagatodo.core.network;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class AcceptGzipInterceptor implements Interceptor {
    private static final String ACCEPT_ENCODING = "gzip";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP = "gzip";

    public AcceptGzipInterceptor() {
    }

    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain
                .request()
                .newBuilder()
                .method(chain.request().method(),gzip(chain.request().body()))
                .addHeader(HEADER_ACCEPT_ENCODING, ACCEPT_ENCODING)
                .addHeader(HEADER_CONTENT_ENCODING, GZIP)
                .build();
        return chain.proceed(request);
    }


    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override public MediaType contentType() {
                return body.contentType();
            }

            @Override public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }

}
