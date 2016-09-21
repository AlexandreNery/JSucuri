/**
 * Created by alexandrenery on 9/20/16.
 */

class Oper
{
    Integer wid; //id of the worker that produced the oper
    Integer dstid; //id of the target task
    Integer dstport; //input port of the target task
    Object value; //actual value of the operand
    Integer tag;
    boolean request_task;

    public Oper(Integer wid, Integer dstid, Integer dstport, Object value)
    {
        this.wid = wid;
        this.dstid = dstid;
        this.dstport = dstport;
        this.value = value;

        this.tag = 0; //default tag
        this.request_task = true; //if true, piggybacks a request for a task to the worker where the opers were produced
    }

/*
    @Override
    public int compare(Oper o1, Oper o2)
    {
        if(o1.tag > o2.tag)
        {
            return 1;
        }
        else if(o1.tag < o2.tag)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
*/

    /*
    @Override
    public int compareTo(Oper other)
    {
        if(!(other instanceof Oper))
        {
            System.out.println("can only compare Oper with Oper.");
        }
        if(this.tag > other.tag)
        {
            return 1;
        }
        else if(this.tag < other.tag)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
    */

}

