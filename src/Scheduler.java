/**
 * Created by alexandrenery on 9/20/16.
 */

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

class Scheduler implements Comparator
{
    //SynchronousQueue operq;
    PriorityBlockingQueue<List<Oper>> operq;
    DFGraph graph;
    List<Task> tasks;
    List<PipedInputStream> worker_conns;
    List<PipedOutputStream> conn;
    List<Integer> waiting; //list of idle workers
    Integer n_workers;
    List<Integer> pending_tasks;
    List<Worker> workers;
    boolean mpi_enabled;

    public Scheduler(DFGraph graph, Integer n_workers, boolean mpi_enabled)
    {
        this.mpi_enabled = mpi_enabled;
        //this.operq = new SynchronousQueue();
        this.operq = new PriorityBlockingQueue<List<Oper>>(11,this);
        this.workers = new ArrayList<Worker>();
        this.graph = graph;
        this.tasks = new ArrayList<Task>();

        this.worker_conns = new ArrayList<PipedInputStream>();
        this.conn = new ArrayList<PipedOutputStream>();

        this.n_workers = n_workers;
        this.pending_tasks = new ArrayList<Integer>(n_workers);

        for(int i = 0 ; i < n_workers ; i++)
            pending_tasks.add(0);

        this.waiting = new ArrayList<Integer>(n_workers);

        //for(int i = 0 ; i < this.pending_tasks.size() ; i++)
        //    this.pending_tasks.add(i,0);

        for(int i = 0 ; i < n_workers ; i++)
        {
            PipedInputStream worker_conn = new PipedInputStream();
            PipedOutputStream sched_conn = new PipedOutputStream();

            try{
                worker_conn.connect(sched_conn);
                //sched_conn.connect(worker_conn);
            }
            catch( IOException e)
            {
                System.out.println(e + ": could not connect worker and scheduler pipes");
                e.printStackTrace();
            }

            worker_conns.add(worker_conn);
            conn.add(sched_conn);
        }

        for(int i = 0 ; i < n_workers ; i++)
        {
            workers.add(new Worker(this.graph, this.operq, worker_conns.get(i), i)); //i = workerid
        }


    }

    public Integer check_match(Node node)
    {
        for(int i = 0 ; i < node.inport[0].size() ; i++)
        {
            TagVal tv = node.inport[0].get(i);
            int count = 1;

            for(int j = 1 ; j < node.inport.length ; j++)
            {
                List<TagVal> port = node.inport[j];

                if(port.contains(tv))
                    count++;
            }

            if(count == node.inport.length)
                return tv.tag;
        }

        return null;
    }

    public void propagate_op(Oper oper)
    {
        Node dst = this.graph.nodes.get(oper.dstid);

        dst.inport[oper.dstport].add(new TagVal(oper.tag, oper.value));

        Integer tag = check_match(dst);

        if(tag != null)
            issue(dst,tag);
    }

    public Integer check_affinity(Task task)
    {
        Node node = this.graph.nodes.get(task.nodeid);
        return node.affinity;
    }

    public void issue(Node node, Integer tag)
    {
        List<Object> args = new ArrayList<Object>();

        for(int i = 0 ; i < node.inport.length ; i++)
        {
            List<TagVal> port = node.inport[i];

            int pos = 0;
            for(int j = 0 ; j < port.size() ; j++)
            {
                TagVal tv = port.get(j);
                if(tv.tag == tag)
                {
                    pos = j;
                    args.add(tv.val);
                }
            }

            port.remove(pos);
        }

        Task t = new Task(node.nf, node.id, args.toArray());
        tasks.add(t);
    }

    public boolean all_idle()
    {
        return (this.waiting.size() == this.n_workers);
    }

    public void terminate_workers()
    {
        for (int i = 0 ; i < this.workers.size() ; i++)
        {
            Worker w = this.workers.get(i);
            try{
                w.join();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void start()
    {
        for(int i = 0 ; i < this.graph.nodes.size() ; i ++)
        {
            Node n = this.graph.nodes.get(i);
            if(n.inport == null || n.inport.length == 0) //if root node (no input ports)
            {
                Task t = new Task(n.nf,n.id,null);
                this.tasks.add(t);
            }
        }

        for(int i = 0 ; i < this.workers.size() ; i++)
        {
            Worker w = this.workers.get(i);
            System.out.println("Starting worker: " + w);
            w.start();
        }

        System.out.println("Main loop");

        main_loop();
    }

    public void main_loop()
    {
        while(this.tasks.size() > 0 || !all_idle() || this.operq.size() > 0 )
        {
            List<Oper> opermsg = null;
            try {
                opermsg = operq.take();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            finally {
                if(opermsg == null)
                {
                    System.out.println("Error! Opermsg null!");
                    System.exit(0);
                }
            }

            for (int i = 0; i < opermsg.size(); i++) {
                Oper oper = opermsg.get(i);
                if (oper.value != null) {
                    propagate_op(oper);
                }
            }

            Integer wid = opermsg.get(0).wid;

            if(!waiting.contains(wid) && opermsg.get(0).request_task)
            {
                if(pending_tasks.get(wid) > 0)
                    pending_tasks.set(wid,pending_tasks.get(wid) - 1);
                else
                    waiting.add(wid);
            }

            while(tasks.size() > 0 && waiting.size() > 0)
            {
                Task task = tasks.remove(0);
                wid = check_affinity(task);
                if(wid != null)
                {
                    if(waiting.contains(wid))
                        waiting.remove(wid);
                    else
                        pending_tasks.set(wid,pending_tasks.get(wid) + 1);
                }
                else
                {
                    wid = waiting.remove(0);
                }

                if(wid < n_workers)
                {
                    Worker worker = workers.get(wid);
                    ObjectOutputStream oos = null;
                    try{
                        oos = new ObjectOutputStream(conn.get(worker.wid));
                        oos.writeObject(task);
                        //oos.close();
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                /*else (Worker não-local, ou seja, MPI...)
                {

                }*/

            }


        }
        System.out.println("Waiting " + this.waiting.size());
        terminate_workers();
    }

    public int compare(Object o1, Object o2)
    {

        Oper op1 = (Oper) o1;
        Oper op2 = (Oper) o2;

        if(op1.tag > op2.tag)
        {
            return 1;
        }
        else if(op1.tag < op2.tag)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }


}

