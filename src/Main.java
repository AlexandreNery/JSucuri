public class Main {

    public static void main(String[] args) {
	// write your code here
        DFGraph dfg = new DFGraph();

        NodeFunction soma = (Object[] inputs) -> {
            Object[] result;
            result = new Object[1];

            result[0] = ((Integer) inputs[0] + (Integer) inputs[1]);

            return result;
            };

        System.out.println("Testando...");

        Feeder A = new Feeder(new Integer(1));
        Feeder B = new Feeder(new Integer(2));
        Node C = new Node(soma,2);

        dfg.add(A);
        dfg.add(B);
        dfg.add(C);

        A.add_edge(C, 0);
        B.add_edge(C, 1);

        Scheduler sched = new Scheduler(dfg,1,false);
        sched.start();

        //C.run();
    }
}
