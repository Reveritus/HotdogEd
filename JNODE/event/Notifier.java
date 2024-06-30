package jnode.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public enum Notifier {
    INSTANSE;
    
    private Hashtable<Class<? extends IEvent>, List<IEventHandler>> notifyMap = new Hashtable<>();

    Notifier() {
    }

    public void register(Class<? extends IEvent> clazz, IEventHandler handler) {
        if (clazz != null && handler != null) {
            List<IEventHandler> list = this.notifyMap.get(clazz);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(handler);
            this.notifyMap.put(clazz, list);
        }
    }

    public void unregister(Class<? extends IEvent> clazz, IEventHandler handler) {
        List<IEventHandler> list;
        if (clazz != null && handler != null && (list = this.notifyMap.get(clazz)) != null) {
            list.remove(handler);
            this.notifyMap.put(clazz, list);
        }
    }

    public void notify(IEvent event) {
        List<IEventHandler> list = this.notifyMap.get(event.getClass());
        if (list != null) {
            for (IEventHandler handler : list) {
                if (handler != null) {
                    handler.handle(event);
                }
            }
        }
    }
}
