package jsucuri; /**
 * Created by alexandrenery on 9/20/16.
 */

import java.util.*;

public class DFGraph
{
    List<Node> nodes;
    public Integer node_count;

    public DFGraph()
    {
        nodes = new Vector<Node>();
        node_count = 0;
    }

    public void add(Node n)
    {
        n.id = this.node_count;
        this.node_count += 1;
        nodes.add(n);
    }

}

