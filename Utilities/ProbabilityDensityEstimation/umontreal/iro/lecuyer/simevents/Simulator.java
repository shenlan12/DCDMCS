

/*
 * Class:        Simulator
 * Description:  Represents the executive of a discrete-event simulator
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

import umontreal.iro.lecuyer.simevents.eventlist.EventList;
import umontreal.iro.lecuyer.simevents.eventlist.SplayTree;
import umontreal.iro.lecuyer.simprocs.*;
   import java.util.ListIterator;

/**
 * Represents the executive of a discrete-event simulator.
 * This class maintains a simulation
 * clock, an event list, and starts executing the events
 * in the appropriate order.
 * Its methods permit one to start, stop, and (re)initialize the simulation,
 * and read the simulation clock.
 * 
 * <P>
 * Usually, a simulation program uses a single simulation clock which is
 * represented by an instance of this class.
 * For more convenience and compatibility, this class therefore provides
 * a mechanism to construct and return a default simulator
 * which is used when an event is constructed without an explicit reference to a
 * simulator, and when the simulator is accessed through
 * the {@link Sim} class.
 * 
 * <P>
 * Note that this class is NOT thread-safe.  Consequently, if a simulation program
 * uses multiple threads, it should acquire a lock on a simulator (using a
 * <TT>synchronized</TT> block) before accessing its state.
 * Note however, that one can launch many simulations in parallel with as many
 * threads, as long as <SPAN  CLASS="textit">each thread has its own</SPAN> <TT>Simulator</TT>.
 * 
 */
public class Simulator  {

   protected double currentTime = 0.0;
      // The current simulation time (clock).

   protected EventList eventList;
      // The list of future events.
      // Can be changed by the method \texttt{init}.

   protected boolean stopped = true;
      // Becomes true when the simulation has ended
      // (stopped has been called or the event list is empty).

   protected boolean simulating = false;

   protected ContinuousState continuousState = null; 

   /**
    * Represents the default simulator being used by the
    *      class {@link Sim}, and the no-argument constructor of {@link Event}.
    *      This simulator is usually obtained with the
    *      {@link #getDefaultSimulator getDefaultSimulator} method, which initializes it if needed.
    *      But it might also be initialized
    *      differently, e.g., if process-driven simulation is required.
    * 
    */
   public static Simulator defaultSimulator = null;


   /**
    * Constructs a new simulator using a splay tree for the
    *   event list.
    * 
    */
   public Simulator() {
     eventList  = new SplayTree();
   }


   /**
    * Constructs a new simulator using <TT>eventList</TT> for
    *   the event list.
    * 
    */
   public Simulator (EventList eventList) {
     if (eventList == null)
        throw new NullPointerException();
     this.eventList = eventList;
   }


   /**
    * Returns the current value of the simulation clock. 
    * @return the current simulation time
    * 
    */
   public double time() 
   {
      return currentTime;
   }


   /**
    * Reinitializes the simulation executive by clearing up the event
    *    list, and resetting the simulation clock to zero.
    * 
    */
   public void init()  {
     // This has to be done another way in order to separate events and processes.
//      SimProcess.killAll();
      currentTime = 0.0;   eventList.clear();   stopped = false;  simulating = false;
   }


   /**
    * Same as {@link #init init}, but also chooses <TT>evlist</TT> as the
    *     event list to be used.
    *     For example, <TT>init (new DoublyLinked())</TT> initializes the simulation
    *     with a doubly linked linear structure for the event list. To initialize the
    *     current <TT>Simulator</TT> with a not empty eventList is also possible, but
    *     the events scheduled in the eventList will be linked with the current simulator only.
    *   
    * @param evlist selected event list implementation
    * 
    * 
    */
   public void init (EventList evlist)  {
      if (evlist == null)
         throw new NullPointerException();
      eventList = evlist;
      ListIterator iter = eventList.listIterator();
      while(iter.hasNext())
         ((Event)iter.next()).setSimulator(this);
      init();
   }


   /**
    * Gets the currently used event list.
    * 
    * @return the currently used event list
    * 
    */
   public EventList getEventList() {
      return eventList;
   }


   /**
    * Determines if this simulator is currently running, i.e.,
    *   executing scheduled events.
    * 
    */
   public boolean isSimulating() {
       return simulating;
   }


   /**
    * Determines if this simulator was stopped by
    *   an event.  The simulator may still be processing the event which
    *   has called the {@link #stop stop} method; in this case,
    *   {@link #isSimulating isSimulating} returns <TT>true</TT>.
    * 
    */
   public boolean isStopped() {
       return stopped;
   }


   /**
    * Removes the first event from the event list and sets the simulation
    *   clock to its event time.
    * 
    * @return the first planned event, or <TT>null</TT> if there is no such event
    * 
    */
   protected Event removeFirstEvent() {
       if (stopped)
          return null;
       Event ev = eventList.removeFirst();
       if (ev == null)
          return null;
       currentTime = ev.eventTime;
       ev.eventTime = -10.0;
       return ev;
   }


   /**
    * Starts the simulation executive.
    *    There must be at least one <TT>Event</TT> in the
    *    event list when this method is called.
    * 
    */
   public void start ()  {
      if (eventList.isEmpty())
        throw new IllegalStateException ("start() called with an empty event list");
      stopped = false;
      simulating = true;
      Event ev;
      try {
         while ((ev = removeFirstEvent()) != null && !stopped) {
   //      while (!stopped && (ev = eventList.removeFirst()) != null) {
   //          currentTime = ev.eventTime;
   //          ev.eventTime = -10.0;
             ev.actions();
             // if ev is a thread object associated to a process,
             // the control will be transfered to this thread and the
             // executive will be passivated in the actions() method.
         }
      }
      finally {
         stopped = true; simulating = false;
      }
   }


   /**
    * Tells the simulation executive to stop as soon as it takes control,
    *    and to return control to the program that called {@link #start start}.
    *    This program will then continue executing from the instructions right after
    *    its call to {@link #start(.) start} If an {@link Event} is currently executing (and
    *    this event has just called {@link #stop stop}), the executive will take
    *    control when the event terminates its execution.
    * 
    */
   public void stop() 
   {
      stopped = true;
   }


   /**
    * Returns the current state of continuous variables being
    *     integrated during the simulation.
    *     This state is used by the {@link Continuous} class when performing
    *     simulation of continuous variables; it defaults to an empty state,
    *     which is initialized only when this method is called.
    *   
    *   @return continuousState field
    *   
    */
   public ContinuousState continuousState() 
   {
      if (continuousState == null)
         continuousState = new ContinuousState(this);
      return continuousState;
   }


   /**
    * Returns the default simulator instance used by
    *   the deprecated class {@link Sim}.
    *   If this simulator does not exist yet, it is constructed using the
    *   no-argument constructor of this class.
    *   One can specify a different default simulator by setting
    *   the <TT>defaultSimulator</TT> field directly.
    * 
    */
   public static Simulator getDefaultSimulator()  {
      if (defaultSimulator == null)
            defaultSimulator = new Simulator();
      return defaultSimulator;
   } 

}
