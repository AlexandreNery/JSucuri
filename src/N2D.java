import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by marcos on 08/10/16.
 */
public class N2D extends Node {

    NodeFunction nf;
    List<TagVal> inport[];
    List dsts;
    Integer inputn;
    Integer i;
    Integer j;

    public N2D(NodeFunction nf, Integer inputn, Integer i, Integer j){
        this.nf = nf;
        this.inputn = inputn;
        this.i = i;
        this.j = j;

        this.inport = new ArrayList[inputn];
        if(inputn > 0){
            for (int k = 0 ; k < this.inport.length ; k++)
                inport[k] = new ArrayList();
        }

        this.dsts = new ArrayList();
    }

    public void add_edge(Node dst, Integer dstport, Integer srcport)
    {
        //this.dsts.add(new Edge(dst.id, dstport))
        this.dsts.add(new Edge(dst.id, dstport, srcport));
    }

    public void run(Object[] args, Integer workerid, PriorityBlockingQueue operq){
        Object output;
        if(inport.length == 0) {
            args = new Object[2];
            args[args.length-2] = i;
            args[args.length-1] = j;
            output = nf.f(args);
        }else{
            args = new Object[args.length+2];
            args[args.length-1] = i;
            args[args.length] = j;
            output = nf.f(args);
        }
        List opers = create_oper(output, workerid, operq, 0);
    }

    public List create_oper(Object value, Integer workerid, PriorityBlockingQueue operq, Integer tag)
    {
        List opers = new ArrayList();

        if(this.getDsts().size() == 0)
        {
            opers.add(new Oper(workerid, null ,null ,null));
        }
        else
        {
            dsts.forEach((e) -> {
                Edge edge = (Edge) e;
                System.out.println("workerid:" + workerid);
                System.out.println("dstid:" + edge.dst_id );
                System.out.println("dstport:"+ edge.dst_port);
                System.out.println("value[srcport]:" + ((Object[])value)[edge.srcport]);
                Oper oper = new Oper(workerid, edge.dst_id, edge.dst_port, ((Object[])value)[edge.srcport]);
                oper.tag = tag;
                opers.add(oper);
            });
        }
        return opers;
    }

    public List getDsts(){
        return dsts;
    }

}
