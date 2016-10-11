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

        if(inputn > 0){
            this.inport = new ArrayList[inputn];

            for (int k = 0 ; k < this.inport.length ; k++)
                inport[k] = new ArrayList();
        }
        else
        {
            this.inport = null;
        }
    }

    public void add_edge(Node dst, Integer dstport, Integer srcport)
    {
        //this.dsts.add(new Edge(dst.id, dstport))
        this.dsts.add(new Edge(dst.id, dstport));
    }

    public void run(Object[] args, Integer workerid, PriorityBlockingQueue operq){
        Object output;
        if(inport.length == 0) {
            output = nf.f(new Object[0]);
        }else{
            output = nf.f(null);
        }
        List opers = create_oper(output, workerid, operq, 0);
    }

    public List create_oper(Object value, Integer workerid, PriorityBlockingQueue operq, Integer tag)
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
                Oper oper = new Oper(workerid, edge.dst_id, edge.dst_port, ((Object[])value)[edge.srcport]);
                oper.tag = tag;
                opers.add(oper);
            });
        }
        return opers;
    }

}
