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
    private boolean terminate;

    //public Worker(DFGraph graph, SynchronousQueue operand_queue, PipedInputStream conn, int workerid)
    public Worker(DFGraph graph, PriorityBlockingQueue operand_queue, PipedInputStream conn, int workerid)
    {
        this.terminate = false;
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

    public void terminate()
    {
        this.terminate = true;
    }

    public PipedInputStream getPipeInput()
    {
        return this.conn;
    }

    @Override
    public void run()
    {
        System.out.println("I am worker " + wid);
        List l = new ArrayList();
        l.add(new Oper(this.wid, null, null, null));

        this.operq.put(l); //request a task to start

        //while(true)
        while(!terminate)
        {
            Task task;
            Node node;
            ObjectInputStream ois = null;

            try{
                if(this.conn.available() > 0) {
                    ois = new ObjectInputStream(this.conn);
                    task = (Task) ois.readObject();
                    //System.out.println("Recv: " + task);
                    node = this.graph.nodes.get(task.nodeid);

                    node.run(task.args, this.wid, this.operq);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("Worker finished!");
    }
}


