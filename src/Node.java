/**
 * Created by alexandrenery on 9/20/16.
 */

import java.util.*;
import java.util.concurrent.*;


class Node
{
    NodeFunction nf;
    List<TagVal> inport[];
    List dsts;
    Integer id;
    Integer affinity;

    public Node()
    {

    }

    public Node(NodeFunction nf, Integer inputn)
    {
        this.nf = nf;

        if(inputn > 0){
            this.inport = new ArrayList[inputn];

            for (int i = 0 ; i < this.inport.length ; i++)
                inport[i] = new ArrayList();
        }
        else
        {
            this.inport = null;
        }

        this.dsts = new ArrayList();
        this.affinity = null;
    }

    public void add_edge(Node dst, Integer dstport)
    {
        //this.dsts.add(new Edge(dst.id, dstport))
        this.dsts.add(new Edge(dst.id, dstport));
    }

    public void pin(Integer workerid)
    {
        this.affinity = workerid;
    }

    //public void run(Object[] args, Integer workerid, SynchronousQueue operq)
    public void run(Object[] args, Integer workerid, ArrayBlockingQueue operq)
    {
        if(inport == null)
        {
            System.out.println("Worker " + workerid + " running node " + id + " with (null args)");
            Object value = this.nf.f(null);
            List opers = create_oper(value, workerid, operq,0); //default tag = 0
            sendops(opers,operq);
        }
        else
        {
            Object value = this.nf.f(args);
            List opers = create_oper(value, workerid, operq,0); //default tag = 0
            sendops(opers,operq);
        }
    }

    //public void sendops(List opers, SynchronousQueue operq)
    public void sendops(List opers, ArrayBlockingQueue operq)
    {
        try {
            operq.put(opers);
        }catch(InterruptedException e)
        {
            System.out.println("operq.put error: " + e);
        }
    }

    //public List create_oper(Object value, Integer workerid, SynchronousQueue operq, Integer tag)
    public List create_oper(Object value, Integer workerid, ArrayBlockingQueue operq, Integer tag)
    {
        List opers = new ArrayList();

        if(this.getDsts().size() == 0)
        {
            opers.add(new Oper(workerid,null,null,null));
        }
        else
        {
            dsts.forEach((e) -> {
                Edge edge = (Edge) e;
                Oper oper = new Oper(workerid, edge.dst_id, edge.dst_port, value);
                oper.tag = tag;
                opers.add(oper);
            });
        }
        return opers;
    }

     public List<TagVal>[] getInport(){
         return inport;
     }
    public List getDsts(){return dsts;}
}
