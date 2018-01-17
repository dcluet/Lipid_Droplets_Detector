macro "Stack_Editing"{

/*
    QC PERFORMED 2018-01-17
    CLUET DAVID
*/

//Get arguments
Argument = getArgument();
Arguments = split(Argument,"\t");

myStack = Arguments[0];
myStart = parseFloat(Arguments[1]);
myEnd = parseFloat(Arguments[2]);

    selectWindow(myStack);
    N=nSlices+1;

    //Delete all frames present after the ending frame
    for (i=myEnd+1; i <N; i++){
        setSlice(myEnd+1);
        run("Delete Slice");
    }
    //Delete all frames present before the starting one
    for (i=1; i <myStart; i++){
        setSlice(1);
        run("Delete Slice");
    }

}//END MACRO
