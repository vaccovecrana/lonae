package io.vacco.myrmica.maven.xform;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import static java.util.Arrays.*;

public class MmPatchLeft { // TODO move this code over to oruzka.

  public static boolean isWrapperType(Class<?> type) {
    return type == Boolean.class
        || type == Integer.class
        || type == Character.class
        || type == Byte.class
        || type == Short.class
        || type == Double.class
        || type == Long.class
        || type == Float.class;
  }

  public static boolean isPrimitiveOrWrapper(final Class<?> type) {
    if (type == null) { return false; }
    return type.isPrimitive() || isWrapperType(type);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Set onSet(Set s0, Set s1) {
    s1.addAll(s0);
    return s1;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map onMap(Map m0, Map m1) {
    for (Object key : m1.keySet()) {
      if (m0.containsKey(key)) {
        if (m0.get(key) instanceof Map) {
          onMap((Map) m0.get(key), (Map) m1.get(key));
        } else {
          m0.put(key, onObj(m0.get(key), m1.get(key)));
        }
      } else {
        m0.put(key, m1.get(key));
      }
    }
    return m0;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> T onObj(T t0, T t1) {
    if (t0 == null) { return t1; }
    if (t1 == null) { return t0; }
    if (t0.getClass().isArray()) {
      Object[] s = onSet(new HashSet(asList((Object[]) t0)), new HashSet(asList((Object[]) t1))).toArray();
      Object[] a = (Object[]) Array.newInstance(t0.getClass().getComponentType(), s.length);
      System.arraycopy(s, 0, a, 0, s.length);
      return (T) a;
    } else if (t0 instanceof List) {
      return (T) onSet(new HashSet((List) t0), new HashSet((List) t1));
    } else if (t0 instanceof Map) {
      return (T) onMap((Map) t0, (Map) t1);
    } else if (t0 instanceof Set) {
      return (T) onSet((Set) t0, (Set) t1);
    } else if (isPrimitiveOrWrapper(t0.getClass()) || t0 instanceof String) {
      return t0;
    } else {
      try {
        for (Field f : t1.getClass().getFields()) {
          Object patched = onObj(f.get(t0), f.get(t1));
          f.set(t1, patched);
        }
      } catch (Exception e) { throw new IllegalStateException(e); }
    }
    return t1;
  }

  public <T> Optional<T> onMultiple(List<T> items) {
    Collections.reverse(items);
    return items.stream().reduce(this::onObj);
  }

  @SafeVarargs public final <T> Optional<T> onMultiple(T... items) {
    return onMultiple(asList(items));
  }
}
