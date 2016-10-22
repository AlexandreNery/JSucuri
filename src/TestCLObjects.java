import com.nativelibs4java.opencl.*;
import org.bridj.Pointer;

import java.io.Serializable;
import java.util.Hashtable;

import static org.bridj.Pointer.pointerToFloats;


/**
 * Created by marcos on 22/10/16.
 */
public class TestCLObjects {
    public static void main(String[] args) {
        // write your code here
        DFGraph dfg = new DFGraph();

        CLContext context = JavaCL.createBestContext();
        final CLQueue queueCL = context.createDefaultQueue();
        Hashtable sm = new Hashtable();
        Pointer<Float> a = pointerToFloats(1,  2,  3,  4 );
        CLBuffer<Float> bufferA = context.createBuffer(CLMem.Usage.Input, Float.class, a.getTargetSize());

        CLBuffer<Float> outBuf = context.createBuffer(CLMem.Usage.Output, Float.class, 4);


        NodeFunction soma = (NodeFunction & Serializable) (Object[] inputs) -> {
            //Integer result;
            //result = new Integer(0);

            //result = ((Integer) inputs[0] + (Integer) inputs[1]);


            CLQueue queue = context.createDefaultQueue();

            String source =
                    "__kernel void addFloats(__global const float* a)     " +
                            "{                                                                                                     " +
                            "   int i = get_global_id(0);                                                                          " +
                            "   output[i] = a[i] + b[i];                                                                           " +
                            "}                                                                                                     ";



            CLKernel kernel = context.createProgram(source).createKernel("addFloats");
            kernel.setArgs(bufferA, outBuf);

            CLEvent copyAEv = bufferA.write(queueCL, a, true);

            CLEvent kernelEv = kernel.enqueueNDRange(queueCL, new int[] { 4 });
            queueCL.finish();

            //sm.put("Porra","Kcete");
            return 0;
        };

        NodeFunction printResult = (NodeFunction & Serializable) (Object[] inputs) -> {

            Pointer<Float> sum = outBuf.read(queueCL);
            for (long i = 0, numEle = sum.getValidElements(); i < numEle; i++)
                System.out.println(sum.get(i));

            //System.out.println("Result: " + inputs[0]);
            //System.out.println(sm);
            //System.out.println("Dado:" + sm.get("Porra"));
            return null;
        };

        System.out.println("Testando...");

        Feeder A = new Feeder(new Integer(1));
        Feeder B = new Feeder(new Integer(2));
        Node C = new Node(soma,2);
        Node D = new Node(printResult,1);

        dfg.add(A);
        dfg.add(B);
        dfg.add(C);
        dfg.add(D);

        A.add_edge(C, 0);
        B.add_edge(C, 1);
        C.add_edge(D, 0);

        Scheduler sched = new Scheduler(dfg,3,false);
        sched.start();


    }
}
