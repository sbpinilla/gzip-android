# GZIP - RETROFIT - COMPRESION / DECOMPRESION

Implementacion de los interceptores de compresion y decompresion para RETROFIT

## AcceptGzipInterceptor

Compresion

## UngzippingInterceptor

Decompresion 

## Implementacion 

Para que la descompresion funcione de forma correcta el "UngzippingInterceptor" debe ser el ultimo  interceptor que se agregue

```
  clientBuilder = OkHttpClient.Builder()
                    .readTimeout(timeOut, TimeUnit.SECONDS)
                    .addInterceptor(AcceptGzipInterceptor())
                    .addInterceptor(UngzippingInterceptor())
```







