

/*
 * Class:        ListWithStat
 * Description:  Implements a list with integrated statistical probes to
                 provide automatic collection of statistics on the sojourn
                 times of objects in the list and on the size of the list
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

package umontreal.iro.lecuyer.simevents;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import umontreal.iro.lecuyer.stat.Tally;
import umontreal.iro.lecuyer.util.TransformingList;
import umontreal.iro.lecuyer.util.PrintfFormat;


/**
 * Implements a list with integrated statistical
 * probes to provide automatic collection of
 * statistics on the sojourn times of objects in the list and on the
 * size of the list as a function of time given by a simulator.
 * The automatic statistical collection can be
 * enabled or disabled for each list, to reduce overhead.
 * This class extends {@link TransformingList TransformingList}
 * and transforms elements into nodes associating insertion times with elements.
 * 
 */
public class ListWithStat<E>
             extends TransformingList<E, ListWithStat.Node<E>> {
   private boolean stats; // true si on a appele setStatCollecting
   private double initTime; // temps de la derniere initialisation
   private Accumulate blockSize; //block stat. sur la longueur de la liste
   private Tally blockSojourn; // block stat. sur les durees de sejour
   private String name;
   private Simulator sim;



   /**
    * Constructs a new list with internal data structure using the default simulator and implemented by <TT>nodeList</TT>.
    *   The given list is cleared for the constructed list to be initially empty.
    * 
    */
   public ListWithStat (List<Node<E>> nodeList) {
      super (nodeList);
      nodeList.clear();
      sim = Simulator.getDefaultSimulator();
      stats = false;
   }


   /**
    * Constructs a new list with internal data structure implemented by
    *   <TT>nodeList</TT>.
    *   The given list is cleared for the constructed list to be initially empty.
    * 
    * @param nodeList the list containing the nodes
    * 
    * 
    */
   public ListWithStat (Simulator inSim, List<Node<E>> nodeList) {
      super (nodeList);
      if (inSim == null || nodeList == null)
          throw new NullPointerException();
      nodeList.clear();
      sim = inSim;
      stats = false;
   }


   /**
    * Constructs a list containing the elements of the specified
    *    collection, whose elements are stored into <TT>nodeList</TT> and using the default simulator.
    * 
    * @param nodeList the list containing the nodes
    * 
    *     @param c collection containing elements to fill in this list with
    * 
    * 
    */
   public ListWithStat (List<Node<E>> nodeList, Collection<? extends E> c) {
      this (Simulator.getDefaultSimulator(), nodeList);
      addAll (c);
   }


   /**
    * Constructs a list containing the elements of the specified
    *    collection, whose elements are stored into <TT>nodeList</TT>.
    * 
    * @param inSim simulator associate to the current variable
    * 
    *     @param nodeList the list containing the nodes
    * 
    *     @param c collection containing elements to fill in this list with
    * 
    * 
    */
   public ListWithStat (Simulator inSim, List<Node<E>> nodeList,
                        Collection<? extends E> c) {
      this (inSim, nodeList);
      addAll (c);
   }


   /**
    * Constructs a new list with name <TT>name</TT>, internal
    *    list <TT>nodeList</TT>, and using the default simulator.
    *    This name can be used to identify the list in traces and reports.
    *    The given list is cleared for the constructed list to be initially empty.
    * 
    * @param nodeList the list containing the nodes
    * 
    *    @param name name for the list object
    * 
    * 
    */
   public ListWithStat (List<Node<E>> nodeList, String name) {
      this (Simulator.getDefaultSimulator(), nodeList);
      this.name = name;
   }


   /**
    * Constructs a new list with name <TT>name</TT>, and
    *   internal list <TT>nodeList</TT>.
    *    This name can be used to identify the list in traces and reports.
    *   The given list is cleared for the constructed list to be initially empty.
    * 
    * @param inSim simulator associate to the current variable
    * 
    *     @param nodeList the list containing the nodes
    * 
    *    @param name name for the list object
    * 
    * 
    */
   public ListWithStat (Simulator inSim, List<Node<E>> nodeList,
                        String name) {
      this (inSim, nodeList);
      this.name = name;
   }


   /**
    * Constructs a new list containing the elements of the
    *    specified collection <TT>c</TT>, with name <TT>name</TT>,
    *    internal list <TT>nodeList</TT>, and using the default simulator.
    *    This name can be used to identify the list in traces and reports.
    * 
    * @param nodeList the list containing the nodes
    * 
    *    @param c collection containing elements to fill in this list with
    * 
    *    @param name name for the list object
    * 
    * 
    */
   public ListWithStat (List<Node<E>> nodeList, Collection<? extends E> c,
                        String name) {
      this (Simulator.getDefaultSimulator(), nodeList);
      this.name = name;
      addAll (c);
   }


   /**
    * Constructs a new list containing the elements of the
    *    specified collection <TT>c</TT>, with name <TT>name</TT>, and
    *    internal list <TT>nodeList</TT>.
    *    This name can be used to identify the list in traces and reports.
    * 
    * @param inSim simulator associate to the current variable
    * 
    *     @param nodeList the list containing the nodes
    * 
    *    @param c collection containing elements to fill in this list with
    * 
    *    @param name name for the list object
    * 
    */
   public ListWithStat (Simulator inSim, List<Node<E>> nodeList,
                        Collection<? extends E> c, String name) {
      this (inSim, nodeList);
      this.name = name;
      addAll (c);
   }


   public E convertFromInnerType (Node<E> node) {
      return node.getElement();
   }

   public Node<E> convertToInnerType (E element) {
      return new Node<E> (element, sim.time());
   }

   /**
    * Returns the simulator associated with this list.
    *  
    * @return the simulator associated with this list
    * 
    */
   public Simulator simulator()  {
      return sim;
   } 


   /**
    * Sets the simulator associated with this list.
    *    This list should be cleared after this method is called.
    *  
    * @param sim the simulator of this list
    * 
    * 
    */
   public void setSimulator(Simulator sim)  {
       if (sim == null)
         throw new NullPointerException();
      this.sim = sim;
      if (blockSize != null)
         blockSize.setSimulator (sim);
   } 


   @Override
   public void clear() {
      if (stats)
         initStat();
      super.clear();
   }

   @Override
   public void add (int index, E obj)  {
      super.add (index, obj);
      if (stats)
         blockSize.update (size());
   }

   @Override
   public E remove (int index) {
      Node<E> node = getInnerList().get (index);
      if (stats)
         blockSojourn.add (sim.time() - node.getInsertionTime());
      E e = super.remove (index);
      if (stats)
         blockSize.update (size());
      return e;
   }

   @Override
   public Iterator<E> iterator() {
      return new IteratorWithStat (getInnerList().iterator());
   }

   @Override
   public ListIterator<E> listIterator() {
      return new ListIteratorWithStat (getInnerList().listIterator());
   }

   @Override
   public ListIterator<E> listIterator (int index) {
      return new ListIteratorWithStat (getInnerList().listIterator (index));
   }

   @Override
   public E set (int index, E element) {
      Node<E> oldNode = getInnerList().get (index);
      E oldElement = oldNode.getElement();
      boolean equal;
      if (oldElement == null || element == null)
         equal = oldElement == element;
      else
         equal = oldElement.equals (element);
      if (equal) {
         getInnerList().set (index, new Node<E> (element, oldNode.getInsertionTime()));
         return oldElement;
      }
      else {
         if (stats)
            blockSojourn.add (sim.time() - oldNode.getInsertionTime());
         getInnerList().set (index, new Node<E> (element, sim.time()));
         return oldElement;
      }
   }

   private class IteratorWithStat implements Iterator<E> {
      private Iterator<Node<E>> itr;
      private Node<E> lastRet;

      public IteratorWithStat (Iterator<Node<E>> itr) {
         this.itr = itr;
      }

      public boolean hasNext() {
         return itr.hasNext();
      }

      public E next() {
         lastRet = itr.next();
         return lastRet.getElement();
      }

      public void remove() {
         itr.remove();
         if (stats) {
            blockSize.update (size());
            blockSojourn.add (sim.time() - lastRet.getInsertionTime());
         }
         lastRet = null;
      }
   }

   private class ListIteratorWithStat implements ListIterator<E> {
      private ListIterator<Node<E>> itr;
      private Node<E> lastRet;

      public ListIteratorWithStat (ListIterator<Node<E>> itr) {
         this.itr = itr;
      }

      public void add (E o) {
         itr.add (new Node<E> (o, sim.time()));
         lastRet = null;
         if (stats)
            blockSize.update (size());
      }

      public boolean hasNext() {
         return itr.hasNext();
      }

      public boolean hasPrevious() {
         return itr.hasPrevious();
      }

      public E next() {
         lastRet = itr.next();
         return lastRet.getElement();
      }

      public int nextIndex() {
         return itr.nextIndex();
      }

      public E previous() {
         lastRet = itr.previous();
         return lastRet.getElement();
      }

      public int previousIndex() {
         return itr.previousIndex();
      }

      public void remove() {
         itr.remove();
         if (stats) {
            blockSize.update (size());
            blockSojourn.add (sim.time() - lastRet.getInsertionTime());
         }
         lastRet = null;
      }

      public void set (E element) {
         if (lastRet == null)
            throw new NoSuchElementException();
         Node<E> oldNode = lastRet;
         E oldElement = oldNode.getElement();
         boolean equal;
         if (oldElement == null || element == null)
            equal = oldElement == element;
         else
            equal = oldElement.equals (element);
         if (equal) {
            lastRet = new Node<E> (element, oldNode.getInsertionTime());
            itr.set (lastRet);
         }
         else {
            if (stats)
               blockSojourn.add (sim.time() - oldNode.getInsertionTime());
            lastRet = new Node<E> (element, sim.time());
            itr.set (lastRet);
         }
      }
   }



   /**
    * Returns <TT>true</TT> if the list collects statistics
    *   about its size and sojourn times of elements, and
    *   <TT>false</TT> otherwise.
    *   By default, statistical collecting is turned off.
    * 
    * @return the status of statistical collecting
    * 
    */
  public boolean getStatCollecting() {
     return stats;
  }


   /**
    * Starts or stops collecting statistics on this list.
    *    If the statistical collection is turned ON, the method
    *    creates two statistical probes if they do not exist yet.
    *    The first one, of the class {@link Accumulate}, measures the evolution
    *    of the size of the list as a function of time.
    *    It can be accessed by the method {@link #statSize statSize}.
    *    The second one, of the class {@link Tally}  and accessible via
    *    {@link #statSojourn statSojourn}, samples the sojourn times in the list of the
    *    objects removed during the observation period,
    *    i.e., between the last initialization time of this statistical
    *    probe and the current time.
    *    The method automatically calls {@link #initStat initStat} to
    *    initialize these two probes.
    *    When this method is used, it is normally invoked immediately after
    *    calling the constructor of the list.
    *   
    * @exception IllegalStateException if the statistical collection
    *      is in the same state as the caller requires
    * 
    * 
    */
  public void setStatCollecting (boolean b) {
    if (b && !stats) {
      if (blockSize == null)
      blockSize = new Accumulate(sim, "List Size " + name);
      if (blockSojourn == null)
      blockSojourn = new Tally("List Sojourn " + name);
      blockSize.update (size());
      stats = true;
      initStat();
    } else
    stats = false;
  }


   /**
    * Reinitializes the two statistical probes created by
    *    {@link #setStatCollecting setStatCollecting}&nbsp;<TT>(true)</TT> and makes an update for the
    *    probe on the list size.
    *   
    * @exception IllegalStateException if the statistical collection is disabled
    * 
    * 
    */
   public void initStat()  {
    if (!stats)
    throw new IllegalStateException("initStat for a list that did not call setStatCollecting (true).");
    blockSize.init();
    blockSojourn.init();
    blockSize.update (size());
    initTime = sim.time();
  }


   /**
    * Returns the last simulation time {@link #initStat initStat} was called.
    * 
    * @return the last simulation time {@link #initStat initStat} was called
    * 
    */
   public double getInitTime() {
      return initTime;
   }


   /**
    * Returns the statistical probe on the evolution of the size of
    *    the list as a function of the simulation time.  This probe
    *    exists only if {@link #setStatCollecting setStatCollecting}&nbsp;<TT>(true)</TT>
    *    has been called for this list.
    *   
    * @return the statistical probe on the evolution of the size of the list
    * 
    */
   public Accumulate statSize()    {
       return blockSize;
   }


   /**
    * Returns the statistical probe on the sojourn times of the objects in
    *    the list.  This probe exists
    *    only if {@link #setStatCollecting setStatCollecting}&nbsp;<TT>(true)</TT> has been called for this list.
    *   
    * @return the statistical probe for the sojourn times in the list
    * 
    */
   public Tally statSojourn()   {
      return blockSojourn;
   }


   /**
    * Returns a string containing a statistical report on the list,
    *    provided that {@link #setStatCollecting setStatCollecting} <TT>(true)</TT> has been
    *    called before for this list.
    *    Even If {@link #setStatCollecting setStatCollecting} was called with <TT>false</TT>
    *    afterward, the report will be made for
    *    the collected observations.
    *    If the probes do not exist, i.e., {@link #setStatCollecting setStatCollecting} was never called
    *    for this object, an illegal state exception will be thrown.
    *   
    * @return a statistical report, represented as a string
    *    @exception IllegalStateException if no statistical probes exist
    * 
    * 
    */
   public String report()   {
        if (blockSojourn == null || blockSize == null)
            throw new IllegalStateException
                ("Calling report when no statistics were collected");

        PrintfFormat str = new PrintfFormat();
        str.append (PrintfFormat.NEWLINE +
            "REPORT ON LIST : ").append (name).append (PrintfFormat.NEWLINE);
        str.append ("   From time: ").append (7, 2, 2, initTime);
        str.append (" to time: ").append (10, 2, 2, sim.time());
        str.append ("                  min        max      average  ");
        str.append ("standard dev.  nb. Obs");

        str.append ("   Size    ");
        str.append (9, (int)(blockSize.min()+0.5));
        str.append (11, (int)(blockSize.max()+0.5));
        str.append (14, 3, 2, blockSize.average()).append(PrintfFormat.NEWLINE);

        str.append ("   Sojourn ");
        str.append ( 12, 3, 2, blockSojourn.min()).append (" ");
        str.append (10, 3, 2, blockSojourn.max()).append (" ");
        str.append (10, 3, 2, blockSojourn.average()).append (" ");
        str.append (10, 3, 2, blockSojourn.standardDeviation()).append (" ");
        str.append (11, blockSojourn.numberObs()).append (PrintfFormat.NEWLINE);

        return str.toString();
    }


   /**
    * Returns the name associated to this list,
    *   or <TT>null</TT> if no name was assigned.
    * 
    * @return the name associated to this list
    * 
    */
   public String getName() {
      return name;
   }


   /**
    * Represents a node that can be part of a list with
    *   statistical collecting.
    * 
    */
   public static class Node<E> {
      private E element;
      private double insertionTime;


   /**
    * Constructs a new node containing element
    *   <TT>element</TT>
    *   inserted into the list at time <TT>insertionTime</TT>.
    * 
    * @param element the element to add into this new node
    * 
    *    @param insertionTime the insertion time of the element
    * 
    * 
    */
      public Node (E element, double insertionTime) {
         this.element = element;
         this.insertionTime = insertionTime;
      }


   /**
    * Returns the element stored into this node.
    * 
    * @return the element into this node
    * 
    */
      public E getElement() { return element; }


   /**
    * Returns the insertion time of the element in this node.
    * 
    * @return the insertion time of the element
    * 
    */
      public double getInsertionTime() { return insertionTime; }


      public String toString() {
         String str = element == null ? "null" : element.toString();
         str += " (inserted at time " + insertionTime + ")";
         return str;
      }
   }

}
