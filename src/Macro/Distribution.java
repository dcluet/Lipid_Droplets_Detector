macro "Distribution" {

    Argument = getArgument();
    Arguments = split(Argument, "\t");

    BinMin = parseFloat(Arguments[0]);
    BinMax = parseFloat(Arguments[1]);
    nBins = parseFloat(Arguments[2]);
    Xaxis = Arguments[3];
    Path = Arguments[4];
    Nom = Arguments[5];
    mySetR = Arguments[6];

    //Convert string set to float
    mySetR = split(mySetR, "-");
    mySet = newArray();
    for (s=0; s<mySetR.length; s++){
        mySet = Array.concat(mySet, parseFloat(mySetR[s]));
    }

    //Create the bins arrays
    binSize = (BinMax-BinMin)/nBins;
    binBlocks = newArray();
    binValues = newArray();

    for (b=BinMin; b<=BinMax; b+=binSize){
        binBlocks = Array.concat(binBlocks, b);
    }

    for (index=0; index<binBlocks.length-1; index++){
        v = (binBlocks[index] + binBlocks[index+1])/2;
        binValues = Array.concat(binValues, v);
    }

    //reOrder mySet
    mySet = Array.sort(mySet);

    //Determine the distribution
    Results = newArray()
    j = 0;Xaxis

    for (max = 1; max <binBlocks.length; max++){
        nbElements = 0;
        for (i=j; i<mySet.length; i++){
            if ((mySet[i]<=binBlocks[max]) && (mySet[i]>binBlocks[max-1])){
                nbElements += 1;
            }else{
                //Break the loop
                j = i;
                i = mySet.length * 10;
            }
        }
        //Update Results
        Results =  Array.concat(Results, nbElements);
    }

    Plot.create("Distribution",
                Xaxis,
                "Counts",
                binValues,
                Results);
    Plot.setFrameSize(1000, 500);
    Plot.show();

    W = getWidth();
    H = getHeight();

    makeRectangle(0,0,W,H);
    run("Copy");
    newImage("Untitled", "8-bit white", W, H, 1);
    run("Paste");
    selectWindow("Distribution");
    close();
    saveAs("Jpeg",
            Path + Nom + "_Distribution.jpg");
    close();

    /*
        Create CSV file
    */


}//END macro
