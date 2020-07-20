### 示例
```
public static void main(String[] args) throws InterruptedException {

    ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>();
    threadLocal.set(true);

    Thread t1 = new Thread(() -> {
        threadLocal.set(false);
        System.out.println(threadLocal.get());
    });

    Thread t2 = new Thread(() -> {
        System.out.println(threadLocal.get());
    });

    t1.start();
    t2.start();
    t1.join();
    t2.join();
    System.out.println(threadLocal.get());
}
```
输出结果：
```
false
null
true
```
可以看到，我们在不同的线程中调用同一个 ThreadLocal 的 get() 方法，获得的值是不同的，看起来就像 ThreadLocal 为每个线程分别存储了不同的值。

### ThreadLocal
首先 ThreadLocal 是一个泛型类，public class ThreadLocal<T>，支持存储各种数据类型。它对外暴露的方法很少，基本就 get() 、set() 、remove() 这三个。下面依次来看一下。

#### set
```
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t); // 获取当前线程的 ThreadLocalMap，等下介绍这个数据结构
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value); // 创建 ThreadLocalMap
}
```
首先通过getMap方法获取当前线程的ThreadLocalMap。
```
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```
原来 Thread 还有这么一个变量 threadLocals ：
```
/*
* 存储线程私有变量，由 ThreadLocal 进行管理
*/
ThreadLocal.ThreadLocalMap threadLocals = null;
```
默认为 null，所以第一次调用时返回 null ，调用 createMap(t, value) 进行初始化：
```
void createMap(Thread t, T firstValue) {
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```
> 每个Thread都有一个ThreadLocalMap对象，这个Map以ThreadLocal对象为key，以要保存的值为value，这样就为每个线程保存了不同的副本。

#### get
set() 方法是向 ThreadLocalMap 中插值，那么 get() 就是在 ThreadLocalMap 中取值了。
```
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t); // 获取当前线程的 ThreadLocalMap
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result; // 找到值，直接返回
        }
    }
    return setInitialValue(); // 设置初始值
}
```
首先获取 ThreadLocalMap，在 Map 中寻找当前 ThreadLocal 对应的 value 值。如果 Map 为空，或者没有找到 value，则通过 setInitialValue() 函数设置初始值。
```
private T setInitialValue() {
    T value = initialValue(); // 为 null
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
    return value;
}

protected T initialValue() {
    return null;
}
```
setInitialValue() 和 set() 逻辑基本一致，只不过 value 是 null 而已。这也解释了文章开头的例子会输出 null。当然，在 ThreadLocal 的子类中，我们可以通过重写 setInitialValue() 来提供其他默认值。

### remove
```
public void remove() {
    ThreadLocalMap m = getMap(Thread.currentThread());
    if (m != null)
        m.remove(this);
}
```
remove() 就更简单了，根据键直接移除对应条目。

### ThreadLocalMap
其实ThreadLocalMap才是ThreadLocal的核心。

#### Entry
ThreadLocalMap 是 ThreadLocal 的静态内部类，它没有直接使用 HashMap，而是一个自定义的哈希表，使用数组实现，数组元素是 Entry。
```
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
        super(k);
        value = v;
    }
}
```
Entry 类继承了 WeakReference<ThreadLocal<?>>，我们可以把它看成是一个键值对。键是当前的 ThreadLocal 对象，值是存储的对象。注意 ThreadLocal 对象的引用是弱引用，值对象 value 的引用是强引用。   

Thread 持有 ThreadLocalMap 的强引用，ThreadLocalMap 中的 Entry 的键是 ThreadLocal 引用。如果线程长期存活或者使用了线程池，而 ThreadLocal 在外部又没有任何强引用了，这种情况下如果 ThreadLocalMap 的键仍然使用强引用 ThreadLocal，就会导致 ThreadLocal 永远无法被垃圾回收，造成内存泄露。

key是弱引用可以使ThreadLocal被GC回收，但是value是强引用，仍然会发生泄漏。当然，JDK也帮我们处理了key被回收的情况。在ThreadLocalMap的set方法中，如果key被GC回收，即key==null，value会被替代。
```
private void set(ThreadLocal<?> key, Object value) {

    // We don't use a fast path as with get() because it is at
    // least as common to use set() to create new entries as
    // it is to replace existing ones, in which case, a fast
    // path would fail more often than not.

    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1); // 当前 key 的哈希，即在数组 table 中的位置

    for (Entry e = tab[i];
        e != null; // 循环直到碰到空 Entry
        e = tab[i = nextIndex(i, len)]) {
        ThreadLocal<?> k = e.get();

        if (k == key) { // 更新 key 对应的值
            e.value = value;
            return;
        }

        if (k == null) { // 替代过期 entry
            replaceStaleEntry(key, value, i);
            return;
        }
    }

    tab[i] = new Entry(key, value);
    int sz = ++size;
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```
当然，最好的避免内存泄漏方式，还是当我们不需要使用ThreadLocal中的数据时，调用下ThreadLocal.remove()方法。

### 总结
通过Thread的内部成员ThreadLocalMap保证每个线程保存一份独立的数据。另外ThreadLocalMap<ThreadLocal, T value>的数据结构，使每个Thread可以有多种类型的独立数据。（即将Thread的内部成员定义为ThreadLocalMap类型，而不是Object类型的原因）。
