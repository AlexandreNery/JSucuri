import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Created by marcos on 08/10/16.
 */
public class LCS {

    public static void main(String[] args) {

        NodeFunction printLCS = (NodeFunction & Serializable) (Object[] inputs) -> {

            System.out.println("Score: " + ((Object[])(inputs[0]))[-1]);
            return null;
        };

        NodeFunction compute = (NodeFunction & Serializable) (Object[] inputs) -> {

            System.out.println("Score: " + ((Object[])(inputs[0]))[-1]);
            LCS(i, j, args);
            return null;
        };


        int nprocs = 1;
        DFGraph lcsGraph = new DFGraph();
        Scheduler sched = new Scheduler(lcsGraph, nprocs, false);

        String nameA = "testA1k.txt";//sys.argv[1]
        String nameB = "testB1k.txt";//sys.argv[2]
        /*BufferedReader fA = null;
        BufferedReader fB = null;
        try {
            fA = new BufferedReader(new InputStreamReader(new FileInputStream(nameA)));
            fB = new BufferedReader(new InputStreamReader(new FileInputStream(nameB)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        String sA = "";
        String sB = "";
        try {
        sA = new String(Files.readAllBytes(Paths.get(nameA)));
        sB = new String(Files.readAllBytes(Paths.get(nameB)));


        } catch (IOException e) {
            e.printStackTrace();
        }


        int sizeA = sA.length() - sA.charAt(-1) == '\n'?1:0;
        int sizeB = sB.length() - sB.charAt(-1) == '\n'?1:0;
        System.out.println("Sizes" +"("+sizeB+","+sizeA+")");
        nprocs = 1;//int(sys.argv[3])
        int block = 1;//int(sys.argv[4])
        int gH = 0;//int(math.ceil(float(sizeB)/block));
        int gW = 0;//int(math.ceil(float(sizeA)/block));
        System.out.println("Grid "+gH+" x "+gW);


        Object[][]G = new Object[gH][gW]; //[[N2D(compute,inputs(i,j),i,j) for j in xrange(gW)] for i in xrange(gH)]
        for(int i=0; i< gH;i++){
            for(int j=0; j< gW; j++ ){
                G[i][j] = new N2D(compute, inputs(i, j), i, j);
            }

        }

        for(int i=0; i < gH;i++) {
            for (int j = 0; j < gW; j++) {
                System.out.println("Node (" +i+","+j+") " +inputs(i,j));
                lcsGraph.add((Node)G[i][j]);
            }
        }

        for(int i=0; i< gH;i++) {
            for (int j = 0; j < gW; j++) {
                if(i > 0){
                    //create edge from  upper neighbor
                    //#print 'Edge (%d,%d) -> (%d,%d)[%d]' % (i-1,j,i,j,0)
                    ((N2D)G[i-1][j]).add_edge(((Node)G[i][j]),0,0);
                }if(j > 0){
                    //create edge from left neighor
                    //#print 'Edge (%d,%d) -> (%d,%d)[%d]' % (i,j-1,i,j, int(i>0))
                    ((N2D)G[i][j-1]).add_edge(G[i][j],int(i>0),1);
                }
            }
        }

        Node R = new Node(printLCS, 1);
        lcsGraph.add(R);
        ((N2D)G[gH-1][gW-1]).add_edge(R,0);
        sched.start();


    }

    static int inputs(int i, int j){
        if(i==0 && j==0)
            return 0;
        if(i==0 || j ==0)
            return 1;
        return 2;
    }
}
