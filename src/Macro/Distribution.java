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
    myResults = newArray();
    j = 0;

    for (max = 1; max <binBlocks.length; max++){
        nbElements = 0;
        for (i=j; i<mySet.length; i++){
            if ((mySet[i]<binBlocks[max]) && (mySet[i]>=binBlocks[max-1])){
                nbElements += 1;
            }else{
                //Break the loop
                j = i;
                i = mySet.length * 10;
            }
        }
        //Update myResults
        myResults =  Array.concat(myResults, nbElements);
    }
    /*
    Plot.create("Distribution",
                Xaxis,
                "Counts",
                binValues,
                myResults);
    Plot.setFrameSize(1000, 500);
    */
    //myEquation = "y = a*exp(-(b-x)*(b-x)/(2*c*c))";
    myEquation = "y = a*exp(-(b-x)*(b-x)/(2*c*c))"; //First gaussian
    myEquation += "" + " + d*exp(-(2*b-x)*(2*b-x)/(8*c*c))"; //Second gaussian
    myEquation += "" + " + e*exp(-(3*b-x)*(3*b-x)/(18*c*c))"; //Third gaussian
    nomParam = newArray("a", "b", "c", "d", "e");
    rankPosArr = Array.rankPositions(myResults);
    myPeak = myResults[rankPosArr[rankPosArr.length-1]];
    myPeakPos = binValues[rankPosArr[rankPosArr.length-1]];


    initialGuesses = newArray(0, myPeakPos, 0, 0, 0);
    Fit.doFit(myEquation, binValues, myResults, initialGuesses);
    //Fit.doFit(myEquation, binValues, myResults);
    a = Fit.p(0);
    b = Fit.p(1);
    c = abs(Fit.p(2));
    d = Fit.p(3);
    e = Fit.p(4);
    myTitle = "Y = " + a + " * exp( -(" + b  + " - X)^2 / (2 * " + c + "^2) ) ";
    myTitle += "+ " + d + " * exp( -(2 * " + b  + " - X)^2 / (2 * (2 * " + c + ")^2) ) ";
    myTitle += "+ " + e + " * exp( -(3 * " + b  + " - X)^2 / (2 * (3 * " + c + ")^2) ) ";

    myFit = newArray(myResults.length);
    mylegend = "Raw\tGaussian model R2= "+ Fit.rSquared + "\n";

    myCSV = "Gausian model: " + "\t" + myEquation + "\n";
    for (p=0; p<Fit.nParams; p++){
        myCSV+= "" + "\t" + nomParam[p] + "= " + "\t" + d2s(Fit.p(p),6) + "\n";
    }

    for(r=0; r<myResults.length; r++){
        x = binValues[r];
        y = a*exp(-(b-x)*(b-x)/(2*c*c)) + d*exp(-(2*b-x)*(2*b-x)/(2*c*c)) + e*exp(-(3*b-x)*(3*b-x)/(3*c*c));
        myFit[r] = y;
    }



    Plot.create("Distribution",
                Xaxis,
                "Counts",
                binValues,
                myResults);

    Plot.setFrameSize(1000, 500);
    Plot.setColor("blue");
    Plot.addText("myEquation", 0, 0);
    Plot.add("line", binValues, myFit);
    //Plot.setColor("blue");
    Plot.setLegend(myTitle, "top-right")

    Plot.show();


    T = getTitle();
    W = getWidth();
    H = getHeight();

    makeRectangle(0,0,W,H);
    run("Copy");
    newImage("Untitled", "RGB white", W, H, 1);
    run("Paste");
    saveAs("Jpeg",
            Path + Nom + "_Distribution.jpg");
    close();
    selectWindow(T);
    close();

    myCSV += "" + "\t" + "R2=" + "\t" + Fit.rSquared + "\n\n";
    myCSV += "BIN" + "\t" + "Counts" + "\t" + "Gaussian model" + "\n";
    for (bin = 0; bin<binValues.length; bin++){
        myCSV += "" + binValues[bin] + "\t" + myResults[bin] + "\t" + myFit[bin] + "\n";
    }
    File.saveString(myCSV, Path + Nom + "_Distribution.csv");

}//END macro
