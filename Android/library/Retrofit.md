# Retrofit

## 自定义CallAdapter

Retrofit的默认API返回是`Call`类型，可以继承`CallAdapter<R, T>`并通过`addCallAdapterFactory`增加自定义的`CallAdapter`，将`Call`转换为指定的类型，例如我们希望返回的是bolts.Task，从而方便地开启线程和更新UI，以下以2.4.0的retrofit库为例

build.gradle

```groovy
// retrofit
implementation 'com.squareup.retrofit2:retrofit:2.4.0'
// bolts task
implementation 'com.parse.bolts:bolts-tasks:1.4.0'
```

实现后的效果，可以直接返回Task类型，而不是Call类型

```kotlin
interface Api {
    @Headers("Authorization:token ${BuildConfig.GITHUB_ACCESS_TOKEN}")
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Task<List<Repo>>
    // fun listRepos(@Path("user") user: String): Call<List<Repo>>
}
```

[Demo](https://github.com/lizij/JetPackDemo)

### `CallAdapter<R, T>`源码解析

2.4版本`CallAdapter`的源码

```java
/**
 * Adapts a {@link Call} with response type {@code R} into the type of {@code T}. Instances are
 * 将Call返回的R类型的response转换为T类型，必须继承和实现
 * created by {@linkplain Factory a factory} which is
 * {@linkplain Retrofit.Builder#addCallAdapterFactory(Factory) installed} into the {@link Retrofit}
 * instance.
 */
public interface CallAdapter<R, T> {
    /**
   * Returns the value type that this adapter uses when converting the HTTP response body to a Java
   * object. For example, the response type for {@code Call<Repo>} is {@code Repo}. This type
   * 返回response会返回的Java类型
   * eg. 如果T为Task，需要声明为TaskCallAdapter<R> implements CallAdapter<R, Task<R>>
   * is used to prepare the {@code call} passed to {@code #adapt}.
   * <p>
   * Note: This is typically not the same type as the {@code returnType} provided to this call
   * adapter's factory.
   */
    Type responseType();

    /**
   * Returns an instance of {@code T} which delegates to {@code call}.
   * 通过Call，返回T类型的一个实例
   * <p>
   * For example, given an instance for a hypothetical utility, {@code Async}, this instance would
   * return a new {@code Async<R>} which invoked {@code call} when run.
   * <pre><code>
   * &#64;Override
   * public &lt;R&gt; Async&lt;R&gt; adapt(final Call&lt;R&gt; call) {
   *   return Async.create(new Callable&lt;Response&lt;R&gt;&gt;() {
   *     &#64;Override
   *     public Response&lt;R&gt; call() throws Exception {
   *       return call.execute();
   *     }
   *   });
   * }
   * </code></pre>
   */
    T adapt(Call<R> call);

    /**
   * Creates {@link CallAdapter} instances based on the return type of {@linkplain
   * Retrofit#create(Class) the service interface} methods.
   */
    abstract class Factory {
        /**
     * Returns a call adapter for interface methods that return {@code returnType}, or null if it
     * cannot be handled by this factory.
     * 选择CallAdapter并在此返回
     */
        public abstract @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
                                                        Retrofit retrofit);

        /**
     * Extract the upper bound of the generic parameter at {@code index} from {@code type}. For
     * example, index 1 of {@code Map<String, ? extends Runnable>} returns {@code Runnable}.
     */
        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }

        /**
     * Extract the raw class type from {@code type}. For example, the type representing
     * {@code List<? extends Runnable>} returns {@code List.class}.
     */
        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }
    }
}

```

### `TaskCallAdapterFactory`

```java
public final class TaskCallAdapterFactory extends CallAdapter.Factory {

    public static TaskCallAdapterFactory create(){
        return new TaskCallAdapterFactory(null);
    }

    public static TaskCallAdapterFactory create(Executor executor){
        if (executor == null){
            throw new NullPointerException("executor == null");
        }
        return new TaskCallAdapterFactory(executor);
    }

    // custom executor for Call
    private Executor executor;

    private TaskCallAdapterFactory(Executor executor){
        this.executor = executor;
    }

    /**
     * Return TaskCallAdapter if returnType is like Task<T>
     * @param returnType should be like Task<T>
     * @param annotations
     * @param retrofit
     * @return return {@link TaskCallAdapter}
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

        // returnType should be like Task<T>
        if (getRawType(returnType) != Task.class){
            return null;
        }

        if (!(returnType instanceof ParameterizedType)){
            throw new IllegalArgumentException("Task return type must be parameterized"
                                               + " as Task<Foo> or Task<? extends Foo>");
        }

        // innerType is T in Task<T>
        Type innerType = getParameterUpperBound(0, (ParameterizedType) returnType);

        return new TaskCallAdapter(executor, innerType);
    }
}
```

### `TaskCallAdapter`

```java
public class TaskCallAdapter<R> implements CallAdapter<R, Task<R>> {

    private Executor executor;
    private final Type responseType;

    TaskCallAdapter(Executor executor, Type responseType){
        this.executor = executor;
        this.responseType = responseType;
    }

    /**
     * Return the return type of Response
     * eg. If the api declares like "Task<Repo> func()", the responseType is Repo
     * @return
     */
    @Override
    public Type responseType() {
        return responseType;
    }

    /**
     * Use {@link TaskCompletionSource} to set result or error safely
     * Using {@link Task} directly is not supported
     * @param call call in retrofit
     * @return an instance of {@link Task}
     */
    @Override
    public Task<R> adapt(final Call<R> call) {
        final TaskCompletionSource<R> tcs = new TaskCompletionSource<>();

        // use custom executor to get response
        if (executor != null){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response<R> response = call.execute();
                        setResponseResult(response, tcs);
                    }catch (IOException e) {
                        tcs.setError(e);
                    }
                }
            });
        } else {
            // use retrofit default executor to get response
            call.enqueue(new Callback<R>() {
                @Override
                public void onResponse(Call<R> call, Response<R> response) {
                    setResponseResult(response, tcs);
                }

                @Override
                public void onFailure(Call<R> call, Throwable t) {
                    tcs.setError(new Exception(t));
                }
            });
        }

        return tcs.getTask();
    }

    private void setResponseResult(Response<R> response, TaskCompletionSource<R> tcs) {
        try {
            if (response.isSuccessful()){
                tcs.setResult(response.body());
            } else {
                tcs.setError(new HttpException(response));
            }
        } catch (CancellationException e){
            tcs.setCancelled();
        } catch (Exception e){
            tcs.setError(e);
        }
    }
}
```



