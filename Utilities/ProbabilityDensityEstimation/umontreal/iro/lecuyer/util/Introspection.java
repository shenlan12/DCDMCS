
/*
 * Class:        Introspection
 * Description:  Methods for introspection using Java Reflection API.
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       
 * @since

 * SSJ is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or
 * any later version.

 * SSJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * A copy of the GNU General Public License is available at
   <a href="http://www.gnu.org/licenses">GPL licence site</a>.
 */

package umontreal.iro.lecuyer.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;



/**
 * Provides utility methods for introspection using
 * Java Reflection API.
 * 
 */
public class Introspection {
   private Introspection() {}

   /**
    * Returns all the methods declared and inherited
    *  by a class. This is similar to {@link Class#getMethods(()) getMethods}
    *  except that it enumerates non-public methods as well as public ones.
    *  This method uses {@link Class#getDeclaredMethods(()) getDeclaredMethods}
    *  to get the declared methods of <TT>c</TT>.
    *  It also gets the declared methods of superclasses.
    *  If a method is defined in a superclass and overriden
    *  in a subclass, only the overriden method will be
    *  in the returned array.
    * 
    * <P>
    * Note that since this method uses
    *  {@link Class#getDeclaredMethods(()) getDeclaredMethods},
    *  it can throw a {@link SecurityException SecurityException} if
    *  a security manager is present.
    * 
    * @param c the class being processed.
    * 
    *    @return the array of methods.
    * 
    */
   public static Method[] getMethods (Class<?> c) {
      // Creates the set of methods for the class.
      List<Method> lst = internalGetMethods (c);

      // Copy the methods to the array that will be returned.
      return lst.toArray (new Method[lst.size()]);
   }


   private static List<Method> internalGetMethods (Class<?> c) {
      // Inspired from java.lang.Class
      List<Method> methods = new ArrayList<Method>();
      Method[] mt = c.getDeclaredMethods();
      for (int i = 0; i < mt.length; i++)
         methods.add (mt[i]);

      List<Method> inheritedMethods = new ArrayList<Method>();
      Class[] iface = c.getInterfaces();
      for (int i = 0; i < iface.length; i++)
         inheritedMethods.addAll (internalGetMethods (iface[i]));
      if (!c.isInterface()) {
         Class<?> s = c.getSuperclass();
         if (s != null) {
            List<Method> supers = internalGetMethods (s);
            for (Method m : supers) {
               // Filter out concrete implementations of any interface
               // methods.
               if (m != null && !Modifier.isAbstract (m.getModifiers()))
                  removeByNameAndSignature (inheritedMethods, m);
            }
            supers.addAll (inheritedMethods);
            inheritedMethods = supers;
         }
      }
      
      // Filter out all local methods from inherited ones
      for (Method m : methods)
         removeByNameAndSignature (inheritedMethods, m);
      
      for (Method m : inheritedMethods) {
         if (m == null)
            continue;
         if (!methods.contains (m))
            methods.add (m);
      }
      return methods;
   }

   private static void removeByNameAndSignature (List<Method> methods, Method m) {
      for (ListIterator<Method> it = methods.listIterator(); it.hasNext(); ) {
         Method tst = it.next();
         if (tst == null)
            continue;
         if (tst.getName().equals (m.getName()) &&
             tst.getReturnType() == m.getReturnType() &&
             sameSignature (tst, m))
            it.set (null);
      }
   }

   /**
    * Determines if two methods <TT>m1</TT> and <TT>m2</TT>
    *  share the same signature. For the signature to be identical,
    *  methods must have the same
    *  number of parameters and the same parameter types.
    * 
    * @param m1 the first method.
    * 
    *    @param m2 the second method.
    * 
    *    @return <TT>true</TT> if the signatures are the same, <TT>false</TT>
    *     otherwise.
    * 
    */
   public static boolean sameSignature (Method m1, Method m2) {
      Class[] pt1 = m1.getParameterTypes();
      Class[] pt2 = m2.getParameterTypes();
      if (pt1.length != pt2.length)
         return false;
      for (int i = 0; i < pt1.length; i++)
         if (pt1[i] != pt2[i])
            return false;
      return true;
   }


   /**
    * Returns all the fields declared and inherited
    *  by a class. This is similar to {@link Class#getFields(()) getFields}
    *  except that it enumerates non-public fields as well as public ones.
    *  This method uses {@link Class#getDeclaredFields(()) getDeclaredFields}
    *  to get the declared fields of <TT>c</TT>.
    *  It also gets the declared fields of superclasses and implemented
    *  interfaces.
    * 
    * <P>
    * Note that since this method uses
    *  {@link Class#getDeclaredFields(()) getDeclaredFields},
    *  it can throw a {@link SecurityException SecurityException} if
    *  a security manager is present.
    * 
    * @param c the class being processed.
    * 
    *    @return the array of fields.
    * 
    */
   public static Field[] getFields (Class<?> c) {
      // Creates the set of fields for the class.
      List<Field> lst = new ArrayList<Field>();
      processFields (c, lst);
      Set<Class<?>> traversedInterfaces = new HashSet<Class<?>>();
      processInterfaceFields (c, lst, traversedInterfaces);

      if (!c.isInterface()) {
         Class<?> s = c.getSuperclass();
         while (s != null) {
            processFields (s, lst);
            processInterfaceFields (s, lst, traversedInterfaces);
            s = s.getSuperclass();
         }
      }

      // Copy the fields to the array that will be returned.
      return lst.toArray (new Field[lst.size()]);
   }


   private static void processFields (final Class<?> c, final List<Field> lst) {
      Field[] f = c.getDeclaredFields();
      for (int i = 0; i < f.length; i++)
         lst.add (f[i]);
   }

   private static void processInterfaceFields (final Class<?> c, final List<Field> lst, final Set<Class<?>> traversedInterfaces) {
      Class[] iface = c.getInterfaces();
      for (int i = 0; i < iface.length; i++) {
         if (traversedInterfaces.contains (iface[i]))
            continue;
         traversedInterfaces.add (iface[i]);
         processFields (iface[i], lst);
         processInterfaceFields (iface[i], lst, traversedInterfaces);
      }
   }

   /**
    * This is like {@link Class#getMethod((String, Class...)) getMethod}, except that it can return non-public methods.
    * 
    * @param c the class being processed.
    * 
    *    @param name the name of the method.
    * 
    *    @param pt the parameter types.
    * 
    *    @exception NoSuchMethodException if the method cannot be found.
    * 
    * 
    */
   public static Method getMethod (Class<?> c, String name, Class[] pt)
                                   throws NoSuchMethodException  {
      try {
         return c.getDeclaredMethod (name, pt);
      }
      catch (NoSuchMethodException nme) {}
      if (!c.isInterface())
         try {
            Class<?> s = c.getSuperclass();
            if (s != null)
               return getMethod (s, name, pt);
         }
         catch (NoSuchMethodException nme) {}
      Class[] iface = c.getInterfaces();
      for (int i = 0; i < iface.length; i++)
         try {
            return getMethod (iface[i], name, pt);
         }
         catch (NoSuchMethodException nme) {}
      throw new NoSuchMethodException
         ("Cannot find method " + name + " in class " + c.getName());
   }


   /**
    * This is like {@link Class#getField((String)) getField},
    *  except that it can return non-public fields.
    * 
    * <P>
    * Note that since this method uses
    *  {@link Class#getDeclaredField((String)) getDeclaredField},
    *  it can throw a {@link SecurityException SecurityException} if
    *  a security manager is present.
    * 
    * @param c the class being processed.
    * 
    *    @param name the name of the method.
    * 
    *    @exception NoSuchFieldException if the field cannot be found.
    * 
    * 
    */
   public static Field getField (Class<?> c, String name)
                                 throws NoSuchFieldException {
      try {
         return c.getDeclaredField (name);
      }
      catch (NoSuchFieldException nfe) {}
      Class[] iface = c.getInterfaces();
      for (int i = 0; i < iface.length; i++)
         try {
            return getField (iface[i], name);
         }
         catch (NoSuchFieldException nme) {}
      if (!c.isInterface())
         try {
            Class s = c.getSuperclass();
            if (s != null)
               return getField (s, name);
         }
         catch (NoSuchFieldException nfe) {}
      throw new NoSuchFieldException
         ("Cannot find field " + name + " in " + c.getName());
   }


   /**
    * Returns the field name corresponding to the value of
    *  an enumerated type <TT>val</TT>.
    *  This method gets the class of <TT>val</TT> and
    *  scans its fields to find a public static and final field
    *  containing <TT>val</TT>. If such a field is found,
    *  its name is returned. Otherwise, <TT>null</TT> is returned.
    * 
    * @param val the value of the enumerated type.
    * 
    *    @return the field name or <TT>null</TT>.
    * 
    */
   public static String getFieldName (Object val) {
      Class<?> enumType = val.getClass();
      Field[] f = enumType.getFields();
      for (int i = 0; i < f.length; i++) {
         if (Modifier.isPublic (f[i].getModifiers()) &&
             Modifier.isStatic (f[i].getModifiers()) &&
             Modifier.isFinal (f[i].getModifiers())) {
            try {
               if (f[i].get (null) == val)
                  return f[i].getName();
            }
            catch (IllegalAccessException iae) {}
         }
      }
      return null;
   }


   /**
    * Returns the field of class <TT>cls</TT> corresponding to
    *  the name <TT>name</TT>. This method looks for
    *  a public, static, and final field with name <TT>name</TT> and
    *  returns its value. If no appropriate field can be
    *  found, an {@link IllegalArgumentException IllegalArgumentException} is thrown.
    * 
    * @param cls the class to look for a field in.
    * 
    *    @param name the name of field.
    * 
    *    @return the object in the field.
    *    @exception IllegalArgumentException if <TT>name</TT> does
    *     not correspond to a valid field name.
    * 
    * 
    */
   public static <T> T valueOf (Class<T> cls, String name) {
      try {
         Field field = cls.getField (name);
         if (Modifier.isStatic (field.getModifiers()) &&
             Modifier.isFinal (field.getModifiers()) &&
             cls.isAssignableFrom (field.getType()))
            return (T)field.get (null);
      }
      catch (NoSuchFieldException nfe) {}
      catch (IllegalAccessException iae) {}
      throw new IllegalArgumentException ("Invalid field name: " + name);
   }


   /**
    * Similar to {@link #valueOf((Class, String)) valueOf} <TT>(cls, name)</TT>,
    *  with case insensitive field name look-up.
    *  If <TT>cls</TT> defines several fields with the same case insensitive
    *  name <TT>name</TT>, an {@link IllegalArgumentException IllegalArgumentException} is thrown.
    * 
    * @param cls the class to look for a field in.
    * 
    *    @param name the name of field.
    * 
    *    @return the object in the field.
    *    @exception IllegalArgumentException if <TT>name</TT> does
    *     not correspond to a valid field name, or if
    *     the class defines several fields with the same name.
    * 
    */
   public static <T> T valueOfIgnoreCase (Class<T> cls, String name) {
      Field[] fields = cls.getFields();
      T res = null;
      for (int i = 0; i < fields.length; i++) {
         Field field = fields[i];
         if (field.getName().equalsIgnoreCase (name) &&
             Modifier.isStatic (field.getModifiers()) &&
             Modifier.isFinal (field.getModifiers()) &&
             cls.isAssignableFrom (field.getType()))
            try {
               T res2 = (T)field.get (null);
               if (res != null && res2 != null)
                  throw new IllegalArgumentException
                     ("Found more than one field with the same name in class " +
                      cls.getName() +
                      " if case is ignored");
               res = res2;
            }
            catch (IllegalAccessException iae) {}
      }
      if (res == null)
         throw new IllegalArgumentException ("Invalid field name: " + name);
      return res;
   }
}
