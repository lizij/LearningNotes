# Network

[TOC]

# okhttp

## 功能

- get,post请求
- 文件的上传下载
- 加载图片(内部会图片大小自动压缩)
- 支持请求回调，直接返回对象、对象集合
- 支持session的保持

## 优势

- 支持HTTP/2, HTTP/2通过使用多路复用技术在一个单独的TCP连接上支持并发, 通过在一个连接上一次性发送多个请求来发送或接收数据
- 如果HTTP/2不可用, 连接池复用技术也可以极大减少延时
- 支持GZIP, 可以压缩下载体积
- 响应缓存可以直接避免重复请求
- 会从很多常用的连接问题中自动恢复
- 如果您的服务器配置了多个IP地址, 当第一个IP连接失败的时候, OkHttp会自动尝试下一个IP
- OkHttp还处理了代理服务器问题和SSL握手失败问题

## 示例

```java
private final OkHttpClient client = new OkHttpClient();

public void run() throws Exception {
    Request request = new Request.Builder()
        .url("http://publicobject.com/helloworld.txt")
        .build();

    client.newCall(request).enqueue(new Callback() {
        @Override public void onFailure(Request request, Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            System.out.println(response.body().string());
        }
    });
}
```

[OkHttp使用完全教程](https://www.jianshu.com/p/ca8a982a116b)