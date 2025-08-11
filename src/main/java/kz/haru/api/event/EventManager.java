package kz.haru.api.event;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final Map<Class<? extends Event>, List<EventHandler>> handlers = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> cachedMethods = new ConcurrentHashMap<>();

    public void register(Object listener) {
        List<Method> methods = cachedMethods.computeIfAbsent(listener.getClass(), clazz -> {
            List<Method> annotatedMethods = new ArrayList<>();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(EventTarget.class)) {
                    if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        throw new IllegalArgumentException("Invalid event listener method: " + method);
                    }
                    annotatedMethods.add(method);
                }
            }
            return annotatedMethods;
        });

        for (Method method : methods) {
            EventTarget annotation = method.getAnnotation(EventTarget.class);
            Priority priority = annotation.priority();

            EventHandler handler = new EventHandler(listener, method, priority);
            Class<? extends Event> eventType = method.getParameterTypes()[0].asSubclass(Event.class);

            handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
            handlers.get(eventType).sort(Comparator.comparing(EventHandler::priority));
        }
    }

    public void unregister(Object listener) {
        List<Method> methods = cachedMethods.get(listener.getClass());
        if (methods == null) return;

        for (Method method : methods) {
            Class<? extends Event> eventType = method.getParameterTypes()[0].asSubclass(Event.class);
            List<EventHandler> eventHandlers = handlers.get(eventType);

            if (eventHandlers != null) {
                eventHandlers.removeIf(handler -> handler.listener.equals(listener));
                if (eventHandlers.isEmpty()) {
                    handlers.remove(eventType);
                }
            }
        }
    }

    public void callEvent(Event event) {
        List<EventHandler> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (EventHandler handler : eventHandlers) {
                try {
                    handler.invoke(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private record EventHandler(Object listener, Method method, @Getter Priority priority) {
        private EventHandler(Object listener, Method method, Priority priority) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
            this.method.setAccessible(true);
        }

        public void invoke(Event event) throws Exception {
            method.invoke(listener, event);
        }
    }
}