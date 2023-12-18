package ICSI311_Interpreter4;

import java.util.ArrayList;

public class ProgramNode extends Node {
    private ArrayList<BlockNode> beginBlocks;
    private ArrayList<BlockNode> endBlocks;
    private ArrayList<BlockNode> otherBlocks;
    private ArrayList<FunctionDefinitionNode> functionList;
    public ProgramNode(){
        //Initializes all the lists.
        beginBlocks = new ArrayList<BlockNode>();
        endBlocks = new ArrayList<BlockNode>();
        otherBlocks = new ArrayList<BlockNode>();
        functionList = new ArrayList<FunctionDefinitionNode>();
    }

    /*
    Since all members are private, add methods are used to add into the list.
     */
    public void addBeginBlocks(BlockNode bN){
        beginBlocks.add(bN);
    }

    public void addEndBlocks(BlockNode bN){
        endBlocks.add(bN);
    }

    public void addOtherBlocks(BlockNode bN){
        otherBlocks.add(bN);
    }

    public void addFunction(FunctionDefinitionNode fN){
        functionList.add(fN);
    }

    public ArrayList<BlockNode> getBeginBlocks(){
        return beginBlocks;
    }

    public ArrayList<BlockNode> getEndBlocks(){
        return endBlocks;
    }

    public ArrayList<BlockNode> getOtherBlocks(){
        return otherBlocks;
    }

    public ArrayList<FunctionDefinitionNode> getFunctionList(){
        return functionList;
    }
    @Override
    public String toString(){
        String retVal = "";
        //Check if each list is empty or not.
        //If not, then print out the information.
        if(!beginBlocks.isEmpty()){
            retVal += "BEGIN: " + beginBlocks.toString() + "\n";
        }

        if(!endBlocks.isEmpty()){
            retVal += "END: " + endBlocks.toString() + "\n";
        }

        if(!otherBlocks.isEmpty()){
            retVal += "Other: " + otherBlocks.toString() + "\n";
        }

        if(!functionList.isEmpty()){
            retVal += "Function Definition: " + functionList.toString() + "\n";
        }

        return retVal;
    }
}
