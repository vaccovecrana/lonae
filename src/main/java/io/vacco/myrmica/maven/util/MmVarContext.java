package io.vacco.myrmica.maven.util;

import java.util.*;

public class MmVarContext {

  private final List<Map<String, Object>> scopes = new ArrayList<>();
  private final List<Map<String, Object>> freeScopes = new ArrayList<>();

  public MmVarContext() { push(); }

  public MmVarContext set(String name, Object value) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      Map<String, Object> ctx = scopes.get(i);
      if (ctx.isEmpty()) continue;
      if (ctx.containsKey(name)) {
        ctx.put(name, value);
        return this;
      }
    }
    scopes.get(scopes.size() - 1).put(name, value);
    return this;
  }

  public Object get(String name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      Map<String, Object> ctx = scopes.get(i);
      if (ctx.isEmpty()) continue;
      Object value = ctx.get(name);
      if (value != null) return value;
    }
    return null;
  }

  public void push() {
    Map<String, Object> newScope = freeScopes.size() > 0 ? freeScopes.remove(freeScopes.size() - 1) : new HashMap<String, Object>();
    scopes.add(newScope);
  }

  public void pop() {
    Map<String, Object> oldScope = scopes.remove(scopes.size() - 1);
    oldScope.clear();
    freeScopes.add(oldScope);
  }
}
