package com.peregrine.graphql.schema;

import com.peregrine.graphql.schema.json.SchemaModelHandler;
import com.peregrine.graphql.schema.model.SchemaModel;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.ArrayList;

@Component(
    service = SchemaProvider.class,
    immediate = true,
    configurationPolicy = ConfigurationPolicy.OPTIONAL
)
@Designate(ocd = SchemaProviderService.Config.class, factory=true)
public class SchemaProviderService
    implements SchemaProvider
{
    @ObjectClassDefinition(
        name = "Peregrine GraphQL Schema Provider",
        description = "Service that provides GraphQL schemas for Peregrine")
    public @interface Config {
        @AttributeDefinition(
            name = "cacheTimeToLive",
            description="Cache Items Time to Live in Seconds (default: 1 day)")
        int cache_time_to_live() default 24 * 3600;

        @AttributeDefinition(
            name = "cacheIntervalTime",
            description="Cache Entry Check Interval in Seconds (default -1). If <=0 then no checks/threads are made beside when cache is used.")
        int cache_interval_time() default -1;

        @AttributeDefinition(
            name = "cacheMaxEntries",
            description="Cache Maximum Number of Entries (default 20). This number must be >= 1")
        int cache_max_entries() default 20;
    }

    private SchemaCache<String, SchemaModel> schemaCache;

    @Reference
    private SchemaModelHandler schemaModelHandler;

    @Activate
    public void activate(Config configuration) {
        schemaCache = new SchemaCache<>(configuration.cache_time_to_live(), configuration.cache_interval_time(), configuration.cache_max_entries());
    }

    @Override
    public String getSchema(Resource schemaParent) {
        return getSchemaModel(schemaParent).print();
    }

    @Override
    public SchemaModel getSchemaModel(Resource schemaParent) {
        if(schemaCache == null) {
            schemaCache = new SchemaCache<>(24 * 3600, -1, 20);
        }
        SchemaModel answer = schemaCache.get(schemaParent.getPath());
        if(answer == null) {
            answer = schemaModelHandler.convert(schemaParent);
            schemaCache.put(schemaParent.getPath(), answer);
        }
        return answer;
    }

    class SchemaCache<K, T> {
        private final long timeToLive;

        // LRUMap: A Map implementation with a fixed maximum size which removes the least recently used entry if an entry is added when full.
        // The least recently used algorithm works on the get and put operations only.
        // Iteration of any kind, including setting the value by iteration, does not change the order.
        // Queries such as containsKey and containsValue or access via views also do not change the order.
        private final LRUMap schemaCacheMap;

        protected class SchemaCacheObject {

            // currentTimeMillis(): Returns the current time in milliseconds.
            // Note that while the unit of time of the return value is a millisecond,
            // the granularity of the value depends on the underlying operating system and may be larger.
            // For example, many operating systems measure time in units of tens of milliseconds.
            public long lastAccessed = System.currentTimeMillis();
            public T value;

            protected SchemaCacheObject(T value) {
                this.value = value;
            }
        }

        public SchemaCache(long timeToLiveInSeconds, final long timerIntervalInSeconds, int maxItems) {
            this.timeToLive = timeToLiveInSeconds * 1000;

            schemaCacheMap = new LRUMap(maxItems);

            if (timeToLive > 0 && timerIntervalInSeconds > 0) {

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                // Thread: A thread is a thread of execution in a program.
                                // The Java Virtual Machine allows an application to have multiple threads of execution running concurrently.
                                Thread.sleep(timerIntervalInSeconds * 1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            schemaCleanup();
                        }
                    }
                });

                // setDaemon(): Marks this thread as either a daemon thread or a user thread.
                // The Java Virtual Machine exits when the only threads running are all daemon threads.
                // This method must be invoked before the thread is started.
                t.setDaemon(true);

                // start(): Causes this thread to begin execution; the Java Virtual Machine calls the run method of this thread.
                // The result is that two threads are running concurrently:
                // the current thread (which returns from the call to the start method) and the other thread (which executes its run method).
                t.start();
            }
        }

        public void put(K key, T value) {
            synchronized (schemaCacheMap) {

                // put(): Puts a key-value mapping into this map.
                schemaCacheMap.put(key, new SchemaCacheObject(value));
            }
        }

        public T get(K key) {
            synchronized (schemaCacheMap) {
                SchemaCacheObject c;
                c = (SchemaCacheObject) schemaCacheMap.get(key);

                if (c == null)
                    return null;
                else {
                    c.lastAccessed = System.currentTimeMillis();
                    return c.value;
                }
            }
        }

        public void remove(K key) {
            synchronized (schemaCacheMap) {
                schemaCacheMap.remove(key);
            }
        }

        public int size() {
            synchronized (schemaCacheMap) {
                return schemaCacheMap.size();
            }
        }

        public void schemaCleanup() {
            // System: The System class contains several useful class fields and methods.
            // It cannot be instantiated. Among the facilities provided by the System class are standard input, standard output,
            // and error output streams; access to externally defined properties and environment variables;
            // a means of loading files and libraries; and a utility method for quickly copying a portion of an array.
            long now = System.currentTimeMillis();
            ArrayList<K> deleteKey = null;

            synchronized (schemaCacheMap) {
                MapIterator itr = schemaCacheMap.mapIterator();

                // ArrayList: Constructs an empty list with the specified initial capacity.
                // size(): Gets the size of the map.
                deleteKey = new ArrayList<K>((schemaCacheMap.size() / 2) + 1);
                K key = null;
                SchemaCacheObject c = null;

                while (itr.hasNext()) {
                    key = (K) itr.next();
                    c = (SchemaCacheObject) itr.getValue();

                    if (c != null && (now > (timeToLive + c.lastAccessed))) {

                        // add(): Appends the specified element to the end of this list.
                        deleteKey.add(key);
                    }
                }
            }

            for (K key : deleteKey) {
                synchronized (schemaCacheMap) {

                    // remove(): Removes the specified mapping from this map.
                    schemaCacheMap.remove(key);
                }

                // yield(): A hint to the scheduler that the current thread is willing to
                // yield its current use of a processor.
                // The scheduler is free to ignore this hint.
                Thread.yield();
            }
        }
    }
}
