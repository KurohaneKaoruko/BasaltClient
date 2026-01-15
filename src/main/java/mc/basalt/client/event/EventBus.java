package mc.basalt.client.event;

import mc.basalt.client.event.annotations.EventHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final Map<Class<? extends Event>, List<Listener>> listeners = new ConcurrentHashMap<>();

    public static void subscribe(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                if (method.getParameterCount() != 1) continue;
                
                Class<?> eventType = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(eventType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) eventType;
                    
                    if (!listeners.containsKey(eventClass)) {
                        listeners.put(eventClass, new CopyOnWriteArrayList<>());
                    }
                    
                    EventHandler annotation = method.getAnnotation(EventHandler.class);
                    listeners.get(eventClass).add(new Listener(object, method, annotation.priority()));
                    listeners.get(eventClass).sort(Comparator.comparingInt(Listener::getPriority).reversed());
                }
            }
        }
    }

    public static void unsubscribe(Object object) {
        listeners.values().forEach(list -> list.removeIf(listener -> listener.object == object));
    }

    public static void post(Event event) {
        List<Listener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Listener listener : eventListeners) {
                try {
                    listener.method.setAccessible(true);
                    listener.method.invoke(listener.object, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Listener {
        Object object;
        Method method;
        int priority;

        public Listener(Object object, Method method, int priority) {
            this.object = object;
            this.method = method;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }
}
