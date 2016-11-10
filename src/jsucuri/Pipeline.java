package jsucuri;

import java.io.*;

/**
 * Created by marcos on 30/09/16.
 */
public class Pipeline {

    public static void main(String[] args) {
        NodeFunction print_line = (NodeFunction & Serializable) (Object[] inputs) -> {

            System.out.println("Result: " + inputs[0]);
            return null;
        };

        int nprocs = 1;

        DFGraph graph = new DFGraph();
        Scheduler sched = new Scheduler(graph, nprocs, false);
        BufferedReader fp = null;
        try {
            fp = new BufferedReader(new InputStreamReader(new FileInputStream("text.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }


        Source src = new Source(fp);
        Node printer = new Serializer(print_line, 1);

        printer.pin(0);
        graph.add(src);

        graph.add(printer);

        src.add_edge(printer, 0);

        sched.start();

    }
}