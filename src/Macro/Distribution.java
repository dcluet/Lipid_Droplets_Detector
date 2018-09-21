macro "Distribution" {

    //Generate distribution curves

    //Deactivate Display
    setBatchMode(true);

    //Retrieve arguments values within an array
    Argument = getArgument();
    Arguments = split(Argument, "*");

    //Minimal value
    BinMin = parseFloat(Arguments[0]);

    //Maximal value
    BinMax = parseFloat(Arguments[1]);

    //Number of bins
    nBins = parseFloat(Arguments[2]);

    //Legend of the X axis
    Xaxis = Arguments[3];

    //Path of the output folder with sample name
    Path = Arguments[4];

    //Name of the distribution
    Nom = Arguments[5];

    //Set of values
    mySetR = Arguments[6];

    //Color of the curve
    mycolor = Arguments[7];

    //Correction value to be applied
    CorrectionS = Arguments[8];

    //Convert values from a Concatenation of strings to an array
    mySetR = split(mySetR, "-");

    //Initialize the array taht will contain the values
    mySet = newArray();

    //Convert string set to float set
    for (s=0; s<mySetR.length; s++){
        mySet = Array.concat(mySet, parseFloat(mySetR[s]));
    }

    //Calculate the size of the bins
    binSize = (BinMax-BinMin)/nBins;

    //Generate an array from the minimal to maximal values to cover
    binBlocks = newArray();
    for (b=BinMin; b<=BinMax; b+=binSize){
        binBlocks = Array.concat(binBlocks, b);
    }

    //Generate an array of mean value of each bin
    binValues = newArray();
    for (index=0; index<binBlocks.length-1; index++){
        v = (binBlocks[index] + binBlocks[index+1])/2;
        binValues = Array.concat(binValues, v);
    }

    //reOrder mySet
    mySet = Array.sort(mySet);

    //Determine the distribution
    myResults = newArray();

    //Initialize the position index within the set of values
    j = 0;

    //Explore the set of bins
    for (max = 1; max <binBlocks.length; max++){

        //Initialize the number of hit for the studied bin
        nbElements = 0;

        //Explore the ordered set of values
        for (i=j; i<mySet.length; i++){

            //If the value is within the limits of the bin
            if ((mySet[i]<=binBlocks[max]) && (mySet[i]>binBlocks[max-1])){

                //nbElement is incremented
                nbElements += 1;

            }else{

                //Else we update the j value to restart at this value
                //for the next bin
                j = i;

                //The program break the for loop
                i = mySet.length * 10;
            }
        }

        //Update myResults
        myResults =  Array.concat(myResults, nbElements);
    }

    //Create the Distribution graph and csv
    createDist();


    //Generate the normalized Cumulative distribution
    myCumulative = newArray();
    j = 0;
    nbElements = 0;
    for (max = 1; max <binBlocks.length; max++){

        for (i=j; i<mySet.length; i++){
            if ((mySet[i]<=binBlocks[max]) && (mySet[i]>binBlocks[max-1])){
                nbElements += 1;
            }else{
                //Break the loop
                j = i;
                i = mySet.length * 10;
            }
        }
        //Update myResults
        myCumulative =  Array.concat(myCumulative, nbElements/CorrectionS);
    }

    //Create the Cumulative graph and csv
    createCumul();

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function createDist(){

    //Create the distribution plot
    Plot.create("Distribution",
                Xaxis,
                "Counts",
                binValues,
                myResults);
    Plot.setColor(mycolor);
    Plot.setFrameSize(1000, 500);
    Plot.addText("Distribution", 0, 0);
    Plot.setColor(mycolor);
    Plot.setLineWidth(3);
    Plot.show();

    //Get properties of the output graph
    T = getTitle();
    W = getWidth();
    H = getHeight();

    //Generate a JPEG file of the graph
    makeRectangle(0,0,W,H);
    run("Copy");
    newImage("Untitled", "RGB white", W, H, 1);
    run("Paste");
    saveAs("Jpeg",
            Path + Nom + "_Distribution.jpg");
    close();
    selectWindow(T);
    close();

    //Create the CSV Distribution file
    myCSV = "";
    myCSV += "BIN" + "\t" + "Counts" + "\n";
    for (bin = 0; bin<binValues.length; bin++){
        myCSV += "" + binValues[bin] + "\t" + myResults[bin] + "\n";
    }
    File.saveString(myCSV, Path + Nom + "_Distribution.csv");

}//END createDist

/*
================================================================================
*/

function createCumul(){

    //Title of the graph
    myTitle = "Cumulative Distribution corrected by " + CorrectionS + " (10⁶ microns²)";

    //Create the distribution plot
    Plot.create("Cumulative Distribution",
                Xaxis,
                "Counts",
                binValues,
                myCumulative);
    Plot.setColor(mycolor);
    Plot.setFrameSize(1000, 500);
    Plot.addText(myTitle, 0, 0);
    Plot.setColor(mycolor);
    Plot.setLineWidth(3);
    Plot.show();

    //Get properties of the output graph
    T = getTitle();
    W = getWidth();
    H = getHeight();

    //Generate a JPEG file of the graph
    makeRectangle(0,0,W,H);
    run("Copy");
    newImage("Untitled", "RGB white", W, H, 1);
    run("Paste");
    saveAs("Jpeg",
            Path + Nom + "_Cumul_Distribution.jpg");
    close();
    selectWindow(T);
    close();

    //Create the CSV Cumulative Distribution
    myCSV2 = "";
    myCSV2 += "BIN" + "\t" + "Cumulative Counts" + "\n";
    for (bin = 0; bin<binValues.length; bin++){
        myCSV2 += "" + binValues[bin] + "\t" + myCumulative[bin] + "\n";
    }
    File.saveString(myCSV2, Path + Nom + "_Cumul_Distribution.csv");

}// END createCumul
