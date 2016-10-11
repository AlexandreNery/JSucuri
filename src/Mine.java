import java.io.*;

/**
 * Created by marcos on 06/10/16.
 */
public class Mine {
    public static void main(String[] args) {
        NodeFunction filterPrices = (NodeFunction & Serializable) (Object[] inputs) -> {
    /*
        prices = []
        filename = args[0]
        base = open("inputs/" + filename, "r")

        regexp = "R\$ [0-9]+\,[0-9]+"
        for line in base:
        for price in re.findall(regexp, line):
        fprice = float(price.replace("R$ ", "").replace(",","."))
        if fprice > 5:
				#print "%s > 5" %fprice
        prices+=[fprice]
        print "%s %s" %(filename, len(prices))
        return len(prices)

        System.out.println("Result: " + inputs[0]);
        */
            System.out.println("Result: " + inputs[0]);
            return null;

        };


        NodeFunction outPrices = (NodeFunction & Serializable) (Object[] inputs) -> {
            System.out.println("prices " + inputs[0]);
            return null;
        };

        int nprocs = 1;

        DFGraph graph = new DFGraph();
        Scheduler sched = new Scheduler(graph, nprocs, false);

        BufferedReader fp = null;
        try {
            fp = new BufferedReader(new InputStreamReader(new FileInputStream("text.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Source src = new Source(fp);

        FilterTagged filter = new FilterTagged(filterPrices, 1);
        Serializer out = new Serializer(outPrices, 1);

        graph.add(src);
        graph.add(filter);
        graph.add(out);

        src.add_edge(filter, 0);
        filter.add_edge(out, 0);

        sched.start();
    }
}