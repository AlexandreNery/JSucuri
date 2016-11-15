package examples.lcs;

import jsucuri.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by marcos on 08/10/16.
 */
public class LCS {

    static int block;
    static int gW;
    static int gH;
    static int sizeA;
    static int sizeB;
    static String sA;
    static String sB;

    public static void main(String[] args) {

        int nprocs = 4;
        DFGraph lcsGraph = new DFGraph();
        Scheduler sched = new Scheduler(lcsGraph, nprocs, false);

        String nameA = "/Users/alexandrenery/IdeaProjects/JSucuri/src/examples/lcs/seqA.txt";//sys.argv[1]
        String nameB = "/Users/alexandrenery/IdeaProjects/JSucuri/src/examples/lcs/seqB.txt";//sys.argv[2]

        try {
            sA = new String(Files.readAllBytes(Paths.get(nameA)));
            sB = new String(Files.readAllBytes(Paths.get(nameB)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        sizeA = sA.length() - (sA.charAt(sA.length()-1) == '\n' ? 1 : 0);
        sizeB = sB.length() - (sB.charAt(sB.length()-1) == '\n' ? 1 : 0);

        System.out.println("Sizes " + sizeB + " x " + sizeA);
        block = 1;

        gH = (int)(Math.ceil((float)(sizeB)/block));
        gW = (int)(Math.ceil((float)(sizeA)/block));

        System.out.println("Grid " + gH + " x " + gW);

        //compute lambda function
        LCSNodeFunction compute = (LCSNodeFunction & Serializable) (Object[] oper, int i, int j) -> {

            int startA = j*block;
            int endA = 0;
            if ((j+1) == gW)
                endA = sizeA;
            else
                endA = startA+block;

            int startB = i*block;
            int endB = 0;
            if ((i+1) == gH)
                endB = sizeB;
            else
                endB = startB+block;
            int lsizeA = endA - startA;
            int lsizeB = endB - startB;

            System.out.println("(" + i + "," + j + ")");
            /*
            System.out.println("lsizeA = " + lsizeA);
            System.out.println("lsizeB = " + lsizeB);
            */

            //print 'jsucuri.Node (%d,%d) calculates (%d,%d) - (%d,%d)' % (i,j, startB,startA,endB,endA)

            int SM[][] = new int[lsizeA+1][lsizeB+1];
            for(int x = 0; x < lsizeA+1; x++){
                for(int y = 0; y < lsizeB+1; y++) {
                    SM[x][y] = 0;
                }
            }

            int port = 0;
            if(i>0) {
                Integer[] op = (Integer[]) oper[0];
                for (int x = 0; x < op.length; x++) {
                    SM[0][x] = op[x];

                }
                port = port + 1;
            }
            if(j>0) {
                Integer[] op = (Integer[]) oper[port];
                for(int x=0; x < op.length; x++) {
                    SM[x][0] = op[x];
                }
                //for(int x=0; x < ((Object[])in[1]).length; x++) {
                //    SM[x][0] = (int)((Object[])in[1])[x];
                //}
            }

            for(int ii = 1; ii <lsizeB+1; ii++){
                for(int jj = 1; jj < lsizeA+1; jj++) {
                    if (sB.charAt(startB + ii - 1) == sA.charAt(startA + jj - 1))
                        SM[ii][jj] = SM[ii - 1][jj - 1] + 1;
                    else
                        SM[ii][jj] = max(SM[ii][jj - 1], SM[ii - 1][jj]);
                }

            }

            //just print SM
            /*System.out.println("---B-SM---");
            for(int k = 0 ; k < SM.length ; k++)
            {
                for(int t = 0 ; t < SM[k].length ; t++)
                {
                    System.out.print(" "  + SM[k][t]);
                }
                System.out.println();
            }
            System.out.println("---E-SM---");*/

            Integer s1[] = new Integer[lsizeA+1];
            Integer s2[] = new Integer[lsizeB+1];

            Integer[][] ret = new Integer[2][];

            for(int q = 0 ; q < SM.length ; q++)
            {
                s1[q] = SM[q][lsizeA];
            }

            for(int q = 0 ; q < SM[0].length ; q++)
            {
                s2[q] = SM[lsizeB][q];
            }

            /*


            int saida[] = new int[lsizeA+1];
            for(int o = 0; o < SM.length; o++){
                saida[o] = SM[o][lsizeA];
            }

            Object[] retorno = new Object[2];
            retorno[0] = SM[lsizeB];
            retorno[1] = saida;

            for (i = 0 ; i < retorno.length ; i ++)
            {
                System.out.println("retorno[" + i + "]:" + retorno[i]);

            }

            return retorno;
            */


            /*System.out.println("s1");
            for(int k = 0 ; k < s1.length ; k++)
                System.out.print(" " + s1[k] + " ");
            System.out.println();

            System.out.println("s2");
            for(int k = 0 ; k < s2.length ; k++)
                System.out.print(" " + s2[k] + " ");
            System.out.println();
            */

            ret[0] = s1;
            ret[1] = s2;

            return ret;

        };


        //building the dataflow graph

        N2D[][]G = new N2D[gH][gW];
        for(int i=0; i< gH;i++){
            for(int j=0; j< gW; j++ ){
                G[i][j] = new N2D(compute, inputs(i, j), i, j);
            }

        }

        for(int i=0; i < gH;i++) {
            for (int j = 0; j < gW; j++) {
                //System.out.println("jsucuri.Node (" +i+","+j+") " +inputs(i,j));
                lcsGraph.add(G[i][j]);
            }
        }

        for(int i=0; i< gH;i++) {
            for (int j = 0; j < gW; j++) {
                if(i > 0){
                    //create edge from  upper neighbor
                    //#print 'jsucuri.Edge (%d,%d) -> (%d,%d)[%d]' % (i-1,j,i,j,0)
                    G[i-1][j].add_edge(G[i][j], 0, 0);
                    //graph+= "N2D_"+(i-1)+"_"+j+ "-> Node_"+i+"_"+j+ "_" + 0 + "_"+0+";\n";

                }if(j > 0){
                    //create edge from left neighor
                    //#print 'jsucuri.Edge (%d,%d) -> (%d,%d)[%d]' % (i,j-1,i,j, int(i>0))
                    G[i][j-1].add_edge(G[i][j], (i > 0 ? 1 : 0) ,1);
                    //graph+= "N2D_"+(i)+"_"+(j-1)+ "-> Node_"+i+"_"+j+ "_" + (i > 0 ? 1 : 0) + "_"+0+";\n";
                }
            }
        }

        //print lambda function
        NodeFunction printLCS = (NodeFunction & Serializable) (Object[] oper) -> {
            //System.out.println("Score: " + ((Object[])(argsn[0]))[-1]);
            Integer[] op = (Integer[]) oper[0];

            System.out.println("Score: " + op[op.length-1]);
            return null;
        };

        Node R = new Node(printLCS, 1);
        lcsGraph.add(R);
        G[gH-1][gW-1].add_edge(R, 0);

        //graph+= "N2D_"+(gH-1)+"_"+(gH-1)+ "-> R_"+ 0 + "_"+0+";\n";
        //System.out.println("Graph\n" +graph);

        //System.out.println("Graph: " + lcsGraph.toString());

        sched.start();


    }

    static int inputs(int i, int j){
        if(i==0 && j==0)
            return 0;
        if(i==0 || j ==0)
            return 1;
        return 2;
    }

/*
    static Object[] LCS(int i, int j, Object[] oper){
        int startA = j*block;
        int endA = 0;
        if ((j+1) == gW)
            endA = sizeA;
        else
            endA = startA+block;

        int startB = i*block;
        int endB = 0;
        if ((i+1) == gH)
            endB = sizeB;
        else
            endB = startB+block;
        int lsizeA = endA - startA;
        int lsizeB = endB - startB;
        //print 'jsucuri.Node (%d,%d) calculates (%d,%d) - (%d,%d)' % (i,j, startB,startA,endB,endA)

        int SM[][] = new int[lsizeA+1][lsizeB+1];
        for(int x = 0; x < lsizeA+1; x++){
            for(int y = 0; y < lsizeB+1; y++) {
                SM[x][y] = 0;
            }
        }

        int port = 0;
        if(i>0) {
            for (int x = 0; x < ((Object[])oper[0]).length; x++) {
                SM[0][x] = (int)((Object[])oper[0])[x];

            }
            port = port + 1;
        }
        if(j>0) {
            for(int x=0; x < ((Object[])oper[port]).length; x++) {
                SM[x][0] = (int)((Object[])oper[port])[x];
            }
        }

        //print 'Antes: Matrix (%d, %d)' %(i,j)
        //printMatrix(SM)
        for(int ii = 1; ii <lsizeB+1; ii++){
            for(int jj = 1; jj < lsizeA+1; jj++) {
                if (sB.charAt(startB + ii - 1) == sA.charAt(startA + jj - 1))
                    SM[ii][jj] = SM[ii - 1][jj - 1] + 1;
                else
                    SM[ii][jj] = max(SM[ii][jj - 1], SM[ii - 1][jj]);
            }

        }

        int saida[] = new int[lsizeA+1];
        for(int o = 0; o < SM.length; o++){
            saida[o] = SM[o][lsizeA];
        }

        Object[] retorno = new Object[2];
        retorno[0] = SM[lsizeB];
        retorno[1] = saida;
        return retorno;
    }*/

    /*
    static Object[] LCS(int i, int j, Object[] oper){
        int startA = j*block;
        int endA = 0;
        if ((j+1) == gW)
            endA = sizeA;
        else
            endA = startA+block;

        int startB = i*block;
        int endB = 0;
        if ((i+1) == gH)
            endB = sizeB;
        else
            endB = startB+block;
        int lsizeA = endA - startA;
        int lsizeB = endB - startB;
        //print 'jsucuri.Node (%d,%d) calculates (%d,%d) - (%d,%d)' % (i,j, startB,startA,endB,endA)

        int SM[][] = new int[lsizeA+1][lsizeB+1];
        for(int x = 0; x < lsizeA+1; x++){
            for(int y = 0; y < lsizeB+1; y++) {
                SM[x][y] = 0;
            }
        }

        int port = 0;
        if(i>0) {

            for (int x = 0; x < ((Object[])oper[0]).length; x++) {
                SM[0][x] = (int)((Object[])oper[0])[x];

            }
            port = port + 1;
        }
        if(j>0) {
            for(int x=0; x < ((Object[])oper[port]).length; x++) {
                SM[x][0] = (int)((Object[])oper[port])[x];
            }
        }

        //print 'Antes: Matrix (%d, %d)' %(i,j)
        //printMatrix(SM)
        for(int ii = 1; ii <lsizeB+1; ii++){
            for(int jj = 1; jj < lsizeA+1; jj++) {
                if (sB.charAt(startB + ii - 1) == sA.charAt(startA + jj - 1))
                    SM[ii][jj] = SM[ii - 1][jj - 1] + 1;
                else
                    SM[ii][jj] = max(SM[ii][jj - 1], SM[ii - 1][jj]);
            }

        }

        int saida[] = new int[lsizeA+1];
        for(int o = 0; o < SM.length; o++){
            saida[o] = SM[o][lsizeA];
        }

        Object[] retorno = new Object[2];
        retorno[0] = SM[lsizeB];
        retorno[1] = saida;
        return retorno;
    }
*/

    static int max(int number, int anotherNumber){
        int max = number;
        if(anotherNumber > number)
            max = anotherNumber;
        return max;
    }

}
