/**
 * Created by alexandrenery on 9/20/16.
 */

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

//Workers receive Tasks from Scheduler and Executes them
class Worker extends Thread
{
    public DFGraph graph;
    //private SynchronousQueue operq;
    public PriorityBlockingQueue operq;
    public PipedInputStream conn; //piped input stream
    public Integer wid;
    public boolean idle;

    //public Worker(DFGraph graph, SynchronousQueue operand_queue, PipedInputStream conn, int workerid)
    public Worker(DFGraph graph, PriorityBlockingQueue operand_queue, PipedInputStream conn, int workerid)
    {
        this.operq = operand_queue;
        this.idle = false;
        this.graph = graph;
        this.wid = workerid;
        this.conn = conn; //it MUST already be connected to the other end

        /*try{
            this.worker_piped_input_stream = new PipedInputStream(sched_piped_output_stream);
        }
        catch(IOException e)
        {
            System.out.println(e);
        }*/
    }

    @Override
    public void run()
    {
        System.out.println("I am worker " + wid);
        List l = new ArrayList();
        l.add(new Oper(this.wid, null, null, null));

        this.operq.put(l); //request a task to start

        while(true)
        {
            Task task;
            Node node;
            ObjectInputStream ois = null;

            try{
                ois = new ObjectInputStream(this.conn);
                task = (Task) ois.readObject();
                node = this.graph.nodes.get(task.nodeid);
                node.run(task.args, this.wid, this.operq);
                //ois.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}


