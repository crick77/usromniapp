/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.service;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public abstract class BaseService {
    private final static String EMPTY_STRING = "";
    
    public String notNull(String s) {
        return s!=null ? s : EMPTY_STRING;
    }
    
    public boolean isEmpty(String s) {
        return s!=null ? s.length()==0 : true;
    }
    
    public boolean isEmpty(Collection<?> c) {
        return (c!=null) ? c.isEmpty() : true;
    }
    
    public boolean isEmpty(Map<?,?> m) {
        return (m!=null) ? m.isEmpty() : true;
    }
    
    public boolean isEmpty(Object[] a) {
        return (a!=null) ? a.length==0 : true;
    }
    
    public String safeTrim(String s) {
        return (s!=null) ? s.trim() : s;
    }
    
    public boolean safeEquals(String a, String b) {
        return Objects.equals(a, b);
    }
    
    public boolean safeEqualsIgnoreCase(String a, String b) {        
        return (b == a) || (a!=null && a.equalsIgnoreCase(b));
    }
}
