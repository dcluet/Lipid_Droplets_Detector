macro "Stack_Editing"{

//Remove unwanted slices from a stack

//Get arguments into an array
Argument = getArgument();
Arguments = split(Argument,"\t");

//Name of the stack window
myStack = Arguments[0];

//Starting slice
myStart = parseFloat(Arguments[1]);

//Ending slice
myEnd = parseFloat(Arguments[2]);

    //Select the current Stack
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
